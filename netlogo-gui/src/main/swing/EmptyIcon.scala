// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.Icon

class EmptyIcon(w: Int, h: Int) extends Icon {
  def getIconWidth  = w
  def getIconHeight = h
  def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {}
}
