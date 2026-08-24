// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension }
import javax.swing.JPanel

class Centered(component: Component) extends JPanel(null) with Transparent with PreferredSize {
  add(component)

  override def getPreferredSize: Dimension =
    component.getPreferredSize

  override def doLayout(): Unit = {
    val size: Dimension = component.getPreferredSize

    component.setBounds(getWidth / 2 - size.width / 2, getHeight / 2 - size.height / 2, size.width, size.height)
  }
}
