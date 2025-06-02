// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ SecondaryLoop, Toolkit }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JFrame, WindowConstants }

import org.nlogo.api.Exceptions
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ ModalProgress, ModalProgressPanel, NetLogoIcon }
import org.nlogo.theme.ThemeSync
import org.nlogo.window.LinkRoot
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events.IconifiedEvent

class AppFrame extends JFrame with LinkParent with LinkRoot with NetLogoIcon with ModalProgress with ThemeSync {
  private var modalProgressPanel = new ModalProgressPanel

  private var modalProgressLoop: Option[SecondaryLoop] = None

  setGlassPane(modalProgressPanel)

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent): Unit = {
      try App.app.fileManager.quit()
      catch { case ex: UserCancelException => Exceptions.ignore(ex) }
    }
    override def windowIconified(e: WindowEvent): Unit = {
      new IconifiedEvent(AppFrame.this, true).raise(App.app)
    }
    override def windowDeiconified(e: WindowEvent): Unit = {
      new IconifiedEvent(AppFrame.this, false).raise(App.app)
    }
  })

  override def showModalProgressPanel(message: String): Unit = {
    modalProgressPanel.setMessage(message)

    getGlassPane.setVisible(true)

    val loop = Toolkit.getDefaultToolkit.getSystemEventQueue.createSecondaryLoop()

    modalProgressLoop = Some(loop)

    loop.enter()
  }

  override def hideModalProgressPanel(): Unit = {
    getGlassPane.setVisible(false)

    modalProgressLoop.foreach(_.exit())

    modalProgressLoop = None
  }

  override def syncTheme(): Unit = {
    App.app.syncWindowThemes()
  }
}
