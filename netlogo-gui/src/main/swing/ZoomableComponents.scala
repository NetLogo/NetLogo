// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Graphics, Insets }
import javax.swing.border.Border

class HorizontalStrut(size: Int) extends Component {
  override def getPreferredSize: Dimension =
    getMinimumSize

  override def getMinimumSize: Dimension =
    new Dimension(Utils.zoom(size), 0)

  override def getMaximumSize: Dimension =
    new Dimension(Utils.zoom(size), Int.MaxValue)
}

class VerticalStrut(size: Int) extends Component {
  override def getPreferredSize: Dimension =
    getMinimumSize

  override def getMinimumSize: Dimension =
    new Dimension(0, Utils.zoom(size))

  override def getMaximumSize: Dimension =
    new Dimension(Int.MaxValue, Utils.zoom(size))
}

class ZoomableBorder(top: Int, left: Int, bottom: Int, right: Int) extends Border {
  override def getBorderInsets(component: Component): Insets =
    new Insets(Utils.zoom(top), Utils.zoom(left), Utils.zoom(bottom), Utils.zoom(right))

  override def isBorderOpaque: Boolean =
    false

  override def paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int): Unit = {}
}
