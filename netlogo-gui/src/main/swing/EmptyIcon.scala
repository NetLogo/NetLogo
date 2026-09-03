// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.Icon

class EmptyIcon(width: Int, height: Int) extends Icon {
  def getIconWidth: Int =
    Utils.zoom(width)

  def getIconHeight: Int =
    Utils.zoom(height)

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {}
}
