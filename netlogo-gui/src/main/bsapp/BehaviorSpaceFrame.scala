// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.JFrame

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ NetLogoIcon, ZoomableWindow }
import org.nlogo.window.{ Event, LinkRoot }

class BehaviorSpaceFrame(app: BehaviorSpaceApp)
  extends JFrame(loc"BehaviorSpace") with NetLogoIcon with LinkRoot with Event.LinkParent with ZoomableWindow(None) {

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      app.abort()
    }
  })
}
