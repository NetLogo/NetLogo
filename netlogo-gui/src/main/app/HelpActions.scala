// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Frame }
import java.awt.event.ActionEvent
import javax.swing.{ Action, AbstractAction }

import org.nlogo.core.I18N
import org.nlogo.api.Version
import org.nlogo.swing.BrowserLauncher
import org.nlogo.swing.UserAction._

class OpenBrowserAction(name: String, url: String, isLocal: Boolean)
  extends AbstractAction(name)
  with MenuAction {
    category = HelpCategory
    group    = HelpWebGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    val launchComponent = e.getSource match {
      case c: Component => c
      case _ => null
    }
    BrowserLauncher.openURL(launchComponent, url, isLocal)
  }
}

object HelpActions {
  def docPath(docName: String): String =
    System.getProperty("netlogo.docs.dir", "docs") + "/" + docName

  def apply: Seq[Action] = {
    Seq(
    new OpenBrowserAction(I18N.gui.get("menu.help.netLogoUserManual"),
      docPath("index.html"), true),
    new OpenBrowserAction(I18N.gui.get("menu.help.netLogoDictionary"),
      docPath("index2.html"), true),
    new OpenBrowserAction(I18N.gui.get("menu.help.netLogoUsersGroup"),
      "http://groups.yahoo.com/group/netlogo-users/", false),
    new OpenBrowserAction(I18N.gui.get("menu.help.introToABM"),
      "https://mitpress.mit.edu/books/introduction-agent-based-modeling", false),
    new OpenBrowserAction(I18N.gui.get("menu.help.donate"),
      "http://ccl.northwestern.edu/netlogo/giving.shtml", false) {
      putValue(ActionGroupKey, HelpAboutGroup)
    })
  }
}

class ShowAboutWindow(frame: Frame)
  extends AbstractAction(I18N.gui.getN("menu.help.aboutVersion", Version.versionDropZeroPatch))
  with MenuAction {
    category = HelpCategory
    group    = HelpAboutGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    println(e.getSource)
    new AboutWindow(frame).setVisible(true)
  }
}
