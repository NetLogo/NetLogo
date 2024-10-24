// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension }
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{ JFrame, WindowConstants }

import org.nlogo.api.Exceptions
import org.nlogo.awt.{ Images, UserCancelException }
import org.nlogo.window.{ LinkRoot, ThemeSync }
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events.IconifiedEvent

class AppFrame extends JFrame with LinkParent with LinkRoot with ThemeSync {
  setIconImage(Images.loadImageResource("/images/arrowhead.gif"))
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  setMinimumSize(new Dimension(300, 300))

  getContentPane.setLayout(new BorderLayout)

  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      try App.app.fileManager.quit()
      catch { case ex: UserCancelException => Exceptions.ignore(ex) }
    }
    override def windowIconified(e: WindowEvent) {
      new IconifiedEvent(AppFrame.this, true).raise(App.app)
    }
    override def windowDeiconified(e: WindowEvent) {
      new IconifiedEvent(AppFrame.this, false).raise(App.app)
    }
  })

  override def syncTheme() {
    App.app.syncWindowThemes()
  }
}
