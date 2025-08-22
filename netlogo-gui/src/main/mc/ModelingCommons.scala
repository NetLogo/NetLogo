// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc

import java.awt.{ Component, Frame }
import java.util.concurrent.TimeoutException

import org.nlogo.api.Workspace
import org.nlogo.core.I18N
import org.nlogo.swing.OptionPane
import org.nlogo.workspace.WorkspaceFactory

import scala.concurrent.Future
import scala.util.Try

import sttp.client4.Backend
import sttp.client4.pekkohttp.PekkoHttpBackend
import sttp.model.headers.CookieWithMeta

object ModelingCommons {
  private val backend = new BackendManager

  private var loginCache: Option[LoginInfo] = None

  def upload(parent: Frame, modelBody: String, workspace: Workspace, workspaceFactory: WorkspaceFactory): Unit = {
    loginCache = loginCache.orElse(new LoginDialog(parent, backend.get()).getLoginInfo)

    loginCache.foreach(new UploadDialog(parent, backend.get(), _, modelBody, workspace, workspaceFactory))
  }

  def logout(parent: Frame): Boolean = {
    loginCache.fold(true) { loginInfo =>
      JsonRequest("/account/logout", loginInfo.cookies, backend.get()).flatMap {
        case JsonResponse(json, _) => Try {
          if (json("status").str != "SUCCESS")
            throw new ServerException

          backend.close()

          loginCache = None
        }
      }.recover(handleError(parent, "logout.failed")).isSuccess
    }
  }

  def displayError(parent: Component, titleKey: String, messageKey: String): Unit = {
    new OptionPane(parent, I18N.gui.get(s"dialog.mc.$titleKey"), I18N.gui.get(s"dialog.mc.$messageKey"),
                   OptionPane.Options.Ok, OptionPane.Icons.Error)
  }

  def handleError(parent: Component, titleKey: String): PartialFunction[Throwable, Unit] = {
    case _: TimeoutException =>
      new OptionPane(parent, I18N.gui.get(s"dialog.mc.$titleKey"), I18N.gui.get("dialog.mc.connection"),
                     OptionPane.Options.Ok, OptionPane.Icons.Error)

    case _: ServerException =>
      new OptionPane(parent, I18N.gui.get(s"dialog.mc.$titleKey"), I18N.gui.get("dialog.mc.response"),
                     OptionPane.Options.Ok, OptionPane.Icons.Error)

    case _: CredentialsException =>
      new OptionPane(parent, I18N.gui.get(s"dialog.mc.$titleKey"), I18N.gui.get("dialog.mc.login.credentials"),
                     OptionPane.Options.Ok, OptionPane.Icons.Error)

    case t => throw t
  }
}

class BackendManager {
  // this backend is required for a Keep-Alive connection, the default one doesn't support it (Isaac B 8/20/25)
  private var backend: Option[Backend[Future]] = None

  def get(): Backend[Future] = {
    backend = backend.orElse(Some(PekkoHttpBackend()))

    backend.get
  }

  def close(): Unit = {
    backend.foreach(_.close())

    backend = None
  }
}

case class LoginInfo(first: String, last: String, id: Int, avatar: String, email: String, groups: Seq[Group],
                     cookies: Seq[CookieWithMeta])

case class Group(id: Int, name: String) {
  override def toString: String =
    name
}

case class ModelEntry(id: Int, name: String) {
  override def toString: String =
    name
}

abstract class Permissions(val id: String, name: String) {
  override def toString: String =
    name
}

case object Everyone extends Permissions("a", I18N.gui.get("dialog.mc.upload.everyone"))
case object OnlyGroup extends Permissions("g", I18N.gui.get("dialog.mc.upload.onlyGroup"))
case object OnlyYou extends Permissions("u", I18N.gui.get("dialog.mc.upload.onlyYou"))

// this class is a catch-all exception for server response issues, which makes
// error handling a bit smoother in the calling code (Isaac B 8/22/25)
class ServerException extends Exception

class CredentialsException extends Exception
