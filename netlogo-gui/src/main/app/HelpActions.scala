// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Frame, KeyboardFocusManager }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import java.net.URI
import java.nio.file.Path
import javax.swing.AbstractAction

import org.nlogo.core.LocaleContext
import org.nlogo.api.Version
import org.nlogo.swing.{ BrowserLauncher, QuickHelp, UserAction }, UserAction._
import org.nlogo.theme.ThemeSync

class TryRemoteBrowseAction(name: String, uri: URI, fallback: Path, group: String, mnemonic: Char)
extends AbstractAction(name)
with MenuAction {
  category   = HelpCategory
  this.group = group
  this.mnemonic = mnemonic

  override def actionPerformed(e: ActionEvent): Unit = {
    BrowserLauncher.tryOpenURI(KeyboardFocusManager.getCurrentKeyboardFocusManager.getFocusedWindow, uri, fallback)
  }
}

class RemoteBrowseAction(name: String, uri: URI, group: String, mnemonic: Char)
extends AbstractAction(name)
with MenuAction {
  category = HelpCategory
  this.group = group
  this.mnemonic = mnemonic

  override def actionPerformed(e: ActionEvent): Unit = {
    BrowserLauncher.openURI(KeyboardFocusManager.getCurrentKeyboardFocusManager.getFocusedWindow, uri)
  }
}

object HelpActions {
  def apply: Seq[MenuAction] = {
    Seq(
    new TryRemoteBrowseAction(loc"NetLogo User Manual",
      new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}"), QuickHelp.docPath("index"), HelpDocGroup, 'U'),
    new TryRemoteBrowseAction(loc"NetLogo Dictionary",
      new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/dictionary.html"),
      QuickHelp.docPath("dictionary"), HelpDocGroup, 'D'),
    new RemoteBrowseAction(loc"Beginner's Interactive NetLogo Dictionary",
      new URI("https://ccl.northwestern.edu/netlogo/bind"), HelpDocGroup, 'B'),
    new RemoteBrowseAction(loc"Introduction to Agent-Based Modeling",
      new URI("https://mitpress.mit.edu/9780262731898/an-introduction-to-agent-based-modeling/"), HelpDocGroup, 'I'),
    new RemoteBrowseAction(loc"NetLogo Forum", new URI("https://forum.netlogo.org"), HelpWebGroup, 'F'),
    new RemoteBrowseAction(loc"NetLogo Users Group", new URI("http://groups.google.com/d/forum/netlogo-users"),
                           HelpWebGroup, 'G') {

      mnemonicIndex = 14
    },
    new RemoteBrowseAction(loc"Contact Us", new URI("https://www.netlogo.org/contact/"), HelpWebGroup, 'C'),
    new RemoteBrowseAction(loc"Donate", new URI("https://www.netlogo.org/donate/"), HelpDonateGroup, 'O'),
    new RemoteBrowseAction(loc"Latest NetLogo News", new URI("https://www.netlogo.org/announcements/"), HelpAboutGroup,
                           'N'))
  }
}

class ShowAboutWindow(frame: Frame)
extends AbstractAction(loc"About ${Version.versionDropZeroPatch}")
with MenuAction with ThemeSync {
  category = HelpCategory
  group    = HelpAboutGroup
  mnemonic = 'A'

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
