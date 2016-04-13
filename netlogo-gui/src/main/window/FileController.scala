// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.{ BrowserLauncher, OptionDialog }
import org.nlogo.workspace.OpenModel.{ Controller => OpenModelController }
import org.nlogo.api.Version
import org.nlogo.core.I18N
import org.nlogo.awt.UserCancelException

import java.nio.file.Paths
import java.net.URI
import java.awt.Component

class FileController(owner: Component) extends OpenModelController {
  // OpenModel.Controller methods
  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    val options = Array[Object](I18N.gui.get("common.buttons.ok"))
    OptionDialog.show(owner, "NetLogo",
      I18N.gui.getN("file.open.error.unableToOpen",
        Paths.get(uri).toString, exception.getMessage),
      options)
    throw new UserCancelException()
  }

  @throws(classOf[IllegalStateException])
  def invalidModel(uri: URI): Unit = {
    throw new IllegalStateException(s"couldn't open: '${uri.toString}'")
  }

  def invalidModelVersion(uri: URI, version: String): Unit = {
    notifyUserNotValidFile()
  }

  def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean = {
    try {
      if (arity == 2)
        checkWithUserBeforeOpening3DModelin2D(version)
      else
        checkWithUserBeforeOpening2DModelin3D()
      true
    } catch {
      case ex: UserCancelException => false
    }
  }

  def shouldOpenModelOfUnknownVersion(version: String): Boolean = {
    try {
      checkWithUserBeforeOpeningModelFromFutureVersion(version);
      true
    } catch {
      case ex: UserCancelException => false
    }
  }

  def shouldOpenModelOfLegacyVersion(version: String): Boolean = {
    showVersionWarningAndGetResponse(version)
  }

  lazy val continueAndCancelOptions = Array[Object](
    I18N.gui.get("common.buttons.continue"),
    I18N.gui.get("common.buttons.cancel"))

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpeningModelFromFutureVersion(version: String): Unit = {
    val message = I18N.gui.getN("file.open.warn.version.newer", Version.version, version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening3DModelin2D(version: String): Unit = {
    val message = I18N.gui.getN("file.open.warn.intwod.openthreed", Version.version, version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening2DModelin3D(): Unit = {
    val message = I18N.gui.getN("file.open.warn.inthreed.opentwod", Version.version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def notifyUserNotValidFile(): Unit = {
    val options = Array[Object](I18N.gui.get("common.buttons.ok"))
    OptionDialog.show(owner, "NetLogo", I18N.gui.get("file.open.error.invalidmodel"), options)
    throw new UserCancelException()
  }

  def showVersionWarningAndGetResponse(version: String): Boolean = {
    val message = I18N.gui.getN("file.open.warn.version.older", version, Version.version)
    val options = Array[Object](
      I18N.gui.get("common.buttons.continue"),
      I18N.gui.get("file.open.warn.version.transitionGuide"),
      I18N.gui.get("common.buttons.cancel"))
    val response =
      OptionDialog.show(owner, I18N.gui.get("common.messages.warning"), message, options)
    response match {
      case 0 => true
      case 1 =>
        BrowserLauncher.openURL(owner, I18N.gui.get("file.open.transitionGuide.url"), false)
        showVersionWarningAndGetResponse(version)
      case 2 => false
    }
  }
}
