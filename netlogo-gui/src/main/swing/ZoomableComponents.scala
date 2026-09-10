// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Graphics, Insets }
import javax.swing.border.Border

class HorizontalStrut(size: Int) extends Zoomable {
  override def getPreferredSize: Dimension =
    getMinimumSize

  override def getMinimumSize: Dimension =
    new Dimension(zoom(size), 0)

  override def getMaximumSize: Dimension =
    new Dimension(zoom(size), Int.MaxValue)
}

class VerticalStrut(size: Int) extends Zoomable {
  override def getPreferredSize: Dimension =
    getMinimumSize

  override def getMinimumSize: Dimension =
    new Dimension(0, zoom(size))

  override def getMaximumSize: Dimension =
    new Dimension(Int.MaxValue, zoom(size))
}

class ZoomableBorder(top: Int, left: Int, bottom: Int, right: Int) extends Border {
  override def getBorderInsets(component: Component): Insets = {
    component match {
      case zoom: ZoomHelpers =>
        new Insets(zoom.zoom(top), zoom.zoom(left), zoom.zoom(bottom), zoom.zoom(right))

      case _ =>
        new Insets(top, left, bottom, right)
    }
  }

  override def isBorderOpaque: Boolean =
    false

  override def paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int): Unit = {}
}
