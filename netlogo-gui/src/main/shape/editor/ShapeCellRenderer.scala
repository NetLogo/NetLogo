// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Component, Dimension, Graphics, Graphics2D, Shape => JShape }
import javax.swing.{ Box, BoxLayout, JLabel, JList, JPanel, ListCellRenderer }

import org.nlogo.api.Graphics2DWrapper
import org.nlogo.core.Shape
import org.nlogo.shape.DrawableShape
import org.nlogo.swing.{ HorizontalStrut, PreferredSize, Utils }
import org.nlogo.theme.InterfaceColors

class ShapeCellRenderer(height: Int) extends JPanel with ListCellRenderer[Shape] {
  protected var shape: Option[DrawableShape] = None
  protected val shapeName = new JLabel

  protected val shapeComponent = new Component with PreferredSize {
    override def getPreferredSize: Dimension =
      new Dimension(Utils.zoom(90), Utils.zoom(ShapeCellRenderer.this.height))

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
      g2d.fillRect(1, 1, getWidth - 2, getHeight - 2)

      preview(g2d, g2d.getClip, Utils.zoom(2), Utils.zoom(12), Utils.zoom(9))
      preview(g2d, g2d.getClip, Utils.zoom(16), Utils.zoom(11), Utils.zoom(12))
      preview(g2d, g2d.getClip, Utils.zoom(33), Utils.zoom(7), Utils.zoom(20))
      preview(g2d, g2d.getClip, Utils.zoom(58), Utils.zoom(2), Utils.zoom(30))
    }
  }

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(shapeComponent)
  add(new HorizontalStrut(20))
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
