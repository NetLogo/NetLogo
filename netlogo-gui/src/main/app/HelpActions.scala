// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Frame }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import java.net.URI
import java.nio.file.Path
import javax.swing.{ Action, AbstractAction }

import org.nlogo.core.I18N
import org.nlogo.api.Version
import org.nlogo.swing.{ BrowserLauncher, UserAction },
  BrowserLauncher.docPath,
  UserAction._
import org.nlogo.theme.ThemeSync

class LocalBrowseAction(name: String, path: Path)
extends AbstractAction(name)
with MenuAction {
  category = HelpCategory
  group    = HelpWebGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    val launchComponent = e.getSource match {
      case c: Component => c
      case _ => null
    }
    BrowserLauncher.openPath(launchComponent, path, "")
  }
}

class RemoteBrowseAction(name: String, uri: URI)
extends AbstractAction(name)
with MenuAction {
  category = HelpCategory
  group    = HelpWebGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    val launchComponent = e.getSource match {
      case c: Component => c
      case _ => null
    }
    BrowserLauncher.openURI(launchComponent, uri)
  }
}

object HelpActions {
  def apply: Seq[Action] = {
    Seq(
    new LocalBrowseAction(I18N.gui.get("menu.help.netLogoUserManual"),
      docPath("index.html")),
    new LocalBrowseAction(I18N.gui.get("menu.help.netLogoDictionary"),
      docPath("index2.html")),
    new RemoteBrowseAction(I18N.gui.get("menu.help.bind"),
      new URI("https://ccl.northwestern.edu/netlogo/bind")),
    new RemoteBrowseAction(I18N.gui.get("menu.help.netLogoUsersGroup"),
      new URI("http://groups.google.com/d/forum/netlogo-users")),
    new RemoteBrowseAction(I18N.gui.get("menu.help.introToABM"),
      new URI("https://mitpress.mit.edu/books/introduction-agent-based-modeling")),
    new RemoteBrowseAction(I18N.gui.get("menu.help.donate"),
      new URI("http://ccl.northwestern.edu/netlogo/giving.shtml")) {
      putValue(ActionGroupKey, HelpAboutGroup)
    })
  }
}

class ShowAboutWindow(frame: Frame)
extends AbstractAction(I18N.gui.getN("menu.help.aboutVersion", Version.versionDropZeroPatch))
with MenuAction with ThemeSync {
  category = HelpCategory
  group    = HelpAboutGroup

  private var aboutWindow: Option[AboutWindow] = None

  override def actionPerformed(e: ActionEvent): Unit = {
    aboutWindow match {
      case Some(window) =>
        window.setVisible(true)

      case None =>
        aboutWindow = Some(new AboutWindow(frame) {
          addWindowListener(new WindowAdapter {
            override def windowClosed(e: WindowEvent) {
              aboutWindow = None
            }
          })

          setVisible(true)
        })
    }
  }

  def syncTheme(): Unit = {
    aboutWindow.foreach(_.syncTheme())
  }
}
