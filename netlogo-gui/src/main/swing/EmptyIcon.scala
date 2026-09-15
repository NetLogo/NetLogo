// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.Icon

class EmptyIcon(zoom: ZoomHelpers, width: Int, height: Int) extends Icon {
  def getIconWidth: Int =
    zoom.zoom(width)

  def getIconHeight: Int =
    zoom.zoom(height)

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {}
}
