// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.BorderLayout
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{ JFrame, WindowConstants }

import org.nlogo.api.Exceptions
import org.nlogo.awt.{ FullScreenUtilities, Images, UserCancelException }
import org.nlogo.window.LinkRoot
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events.IconifiedEvent

class AppFrame extends JFrame with LinkParent with LinkRoot {
  setIconImage(Images.loadImageResource("/images/arrowhead.gif"))
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  getContentPane.setLayout(new BorderLayout)
  FullScreenUtilities.setWindowCanFullScreen(this, true)

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
}
