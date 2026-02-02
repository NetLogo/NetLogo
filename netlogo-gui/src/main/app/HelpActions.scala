// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Frame, KeyboardFocusManager }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import java.net.URI
import java.nio.file.Path
import javax.swing.AbstractAction

import org.nlogo.core.I18N
import org.nlogo.api.Version
import org.nlogo.swing.{ BrowserLauncher, UserAction }, UserAction._
import org.nlogo.theme.ThemeSync
import org.nlogo.window.QuickHelp

class TryRemoteBrowseAction(name: String, uri: URI, fallback: Path, group: String)
extends AbstractAction(name)
with MenuAction {
  category   = HelpCategory
  this.group = group

  override def actionPerformed(e: ActionEvent): Unit = {
    BrowserLauncher.tryOpenURI(KeyboardFocusManager.getCurrentKeyboardFocusManager.getFocusedWindow, uri, fallback)
  }
}

class RemoteBrowseAction(name: String, uri: URI, group: String)
extends AbstractAction(name)
with MenuAction {
  category = HelpCategory
  this.group = group

  override def actionPerformed(e: ActionEvent): Unit = {
    BrowserLauncher.openURI(KeyboardFocusManager.getCurrentKeyboardFocusManager.getFocusedWindow, uri)
  }
}

object HelpActions {
  def apply: Seq[MenuAction] = {
    Seq(
    new TryRemoteBrowseAction(I18N.gui.get("menu.help.netLogoUserManual"),
      new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}"), QuickHelp.docPath(None), HelpDocGroup),
    new TryRemoteBrowseAction(I18N.gui.get("menu.help.netLogoDictionary"),
      new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/dictionary.html"),
      QuickHelp.docPath(Some("dictionary-netlogo-dictionary")), HelpDocGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.bind"),
      new URI("https://ccl.northwestern.edu/netlogo/bind"), HelpDocGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.introToABM"),
      new URI("https://mitpress.mit.edu/9780262731898/an-introduction-to-agent-based-modeling/"), HelpDocGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.forum"),
      new URI("https://forum.netlogo.org"), HelpWebGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.netLogoUsersGroup"),
      new URI("http://groups.google.com/d/forum/netlogo-users"), HelpWebGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.contact"),
      new URI("https://www.netlogo.org/contact/"), HelpWebGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.donate"),
      new URI("https://www.netlogo.org/donate/"), HelpDonateGroup),
    new RemoteBrowseAction(I18N.gui.get("menu.help.news"),
      new URI("https://www.netlogo.org/announcements/"), HelpAboutGroup))
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
            override def windowClosed(e: WindowEvent): Unit = {
              aboutWindow = None
            }
          })

          setVisible(true)
        })
    }
  }

  override def syncTheme(): Unit = {
    aboutWindow.foreach(_.syncTheme())
  }
}
