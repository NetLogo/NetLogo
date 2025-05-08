// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Component, Dimension, Graphics, Graphics2D, Shape => JShape }
import javax.swing.{ Box, BoxLayout, JLabel, JList, JPanel, ListCellRenderer }

import org.nlogo.api.Graphics2DWrapper
import org.nlogo.core.Shape
import org.nlogo.shape.DrawableShape
import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

class ShapeCellRenderer extends JPanel with ListCellRenderer[Shape] {
  protected var shape: Option[DrawableShape] = None
  protected val dimension = new Dimension(90, 34)
  protected val shapeName = new JLabel
  protected val shapeComponent = new Component {
    setMinimumSize(dimension)
    setPreferredSize(dimension)
    setMaximumSize(dimension)

    private def preview(g2d: Graphics2D, clip: JShape, left: Int, top: Int, size: Int): Unit = {
      shape.foreach(shape => {
        g2d.setColor(getForeground)

        if (shape.isRotatable) {
          g2d.fillOval(left - 1, top - 1, size + 1, size + 1)
        } else {
          g2d.fillRect(left - 1, top - 1, size + 2, size + 2)
        }

        g2d.clipRect(left, top, size, size)

        shape.paint(new Graphics2DWrapper(g2d), EditorDialog.getColor(shape.getEditableColorIndex), left, top, size, 0)

        g2d.setClip(clip)
      })
    }

    override def paint(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(getBackground)
      g2d.fillRect(1, 1, dimension.width - 2, dimension.height - 2)

      preview(g2d, g2d.getClip, 2, 12, 9)
      preview(g2d, g2d.getClip, 16, 11, 12)
      preview(g2d, g2d.getClip, 33, 7, 20)
      preview(g2d, g2d.getClip, 58, 2, 30)
    }
  }

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(shapeComponent)
  add(Box.createHorizontalStrut(20))
  add(shapeName)
  add(Box.createHorizontalGlue)

  // Method that actually renders the item
  override def getListCellRendererComponent(list: JList[? <: Shape], value: Shape, index: Int, isSelected: Boolean,
                                            cellHasFocus: Boolean): Component = {
    shape = Option(value.asInstanceOf[DrawableShape])
    shapeName.setText(value.name)

    if (isSelected) {
      setBackground(InterfaceColors.dialogBackgroundSelected())
      shapeName.setForeground(InterfaceColors.dialogTextSelected())
      shapeComponent.setBackground(InterfaceColors.dialogBackgroundSelected())
      shapeComponent.setForeground(InterfaceColors.dialogTextSelected())
    }

    else {
      setBackground(InterfaceColors.dialogBackground())
      shapeName.setForeground(InterfaceColors.dialogText())
      shapeComponent.setBackground(InterfaceColors.dialogBackground())
      shapeComponent.setForeground(InterfaceColors.dialogText())
    }

    this
  }
}
