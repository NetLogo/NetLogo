// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Graphics }
import javax.swing.Icon

class ColorIcon(color: Color, width: Int, height: Int) extends Icon {
  override def getIconWidth = width
  override def getIconHeight = height
  def paintIcon(c: java.awt.Component, g: Graphics, x: Int, y: Int) {
    if (width > 0 && height > 0 ) {
      g.setColor(color)
      g.fillRect(x ,y, x + width, y + height)
    }
  }
}
