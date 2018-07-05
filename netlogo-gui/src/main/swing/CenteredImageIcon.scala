// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.ImageIcon

class CenteredImageIcon(orig: ImageIcon, w: Int, h: Int) extends ImageIcon(orig.getImage) {
  override def getIconWidth  = w
  override def getIconHeight = h
  override def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {
    super.paintIcon(c, g, w/2 - orig.getIconWidth/2, h/2 - orig.getIconHeight/2)
  }
}
