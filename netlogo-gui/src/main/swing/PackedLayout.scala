// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Dimension
import javax.swing.{ Box, BoxLayout, JComponent, JPanel }

object PackedLayout {
  sealed abstract trait Orientation

  case object Horizontal extends Orientation
  case object Vertical extends Orientation

  sealed abstract trait Alignment

  case object Leading extends Alignment
  case object Center extends Alignment
  case object Trailing extends Alignment
}

// this layout creates a tightly packed JPanel with the provided components in a row. intended as a helper
// for creating nested BoxLayouts, which can get rather verbose and repetitive. (Isaac B 2/3/26)
class PackedLayout(components: Seq[JComponent], orientation: PackedLayout.Orientation = PackedLayout.Horizontal,
                   alignment: PackedLayout.Alignment = PackedLayout.Center, spacing: Int = 0)
  extends JPanel with Transparent {

  orientation match {
    case PackedLayout.Horizontal =>
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

      if (alignment == PackedLayout.Trailing)
        add(Box.createHorizontalGlue)

      add(components.head)

      components.tail.foreach { component =>
        add(Box.createHorizontalStrut(spacing))
        add(component)
      }

      if (alignment == PackedLayout.Leading)
        add(Box.createHorizontalGlue)

    case PackedLayout.Vertical =>
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

      if (alignment == PackedLayout.Trailing)
        add(Box.createVerticalGlue)

      add(components.head)

      components.tail.foreach { component =>
        add(Box.createVerticalStrut(spacing))
        add(component)
      }

      if (alignment == PackedLayout.Leading)
        add(Box.createVerticalGlue)
  }

  override def getPreferredSize: Dimension = {
    orientation match {
      case PackedLayout.Horizontal =>
        new Dimension(super.getPreferredSize.width, components.map(_.getPreferredSize.height).max)

      case PackedLayout.Vertical =>
        new Dimension(components.map(_.getPreferredSize.width).max, super.getPreferredSize.height)
    }
  }

  override def getMinimumSize: Dimension = {
    orientation match {
      case PackedLayout.Horizontal =>
        new Dimension(super.getMinimumSize.width, components.map(_.getPreferredSize.height).max)

      case PackedLayout.Vertical =>
        new Dimension(components.map(_.getPreferredSize.width).max, super.getMinimumSize.height)
    }
  }

  override def getMaximumSize: Dimension = {
    orientation match {
      case PackedLayout.Horizontal =>
        new Dimension(super.getMaximumSize.width, components.map(_.getPreferredSize.height).max)

      case PackedLayout.Vertical =>
        new Dimension(components.map(_.getPreferredSize.width).max, super.getMaximumSize.height)
    }
  }
}
