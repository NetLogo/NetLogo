// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.JFrame

import org.nlogo.core.I18N
import org.nlogo.swing.NetLogoIcon
import org.nlogo.window.{ Event, LinkRoot }

class BehaviorSpaceFrame(app: BehaviorSpaceApp)
  extends JFrame(I18N.gui.get("menu.tools.behaviorSpace")) with NetLogoIcon with LinkRoot with Event.LinkParent {

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      app.abort()
    }
  })
}
