// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Container, Dimension, Font, Graphics, LayoutManager }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.plot.PlotPen
import org.nlogo.swing.Transparent
import org.nlogo.theme.InterfaceColors

class PlotLegend(widget: AbstractPlotWidget) extends JPanel(new WrapLayout) with Transparent {
  private var boldState: Int = Font.PLAIN

  var open = false

  def addPen(pen: PlotPen): Unit = {
    if (open) {
      if (pen.inLegend)
        add(new LegendItem(pen))

      revalidate()
    }
  }

  def toggle(): Unit = { open = !open; refresh() }
  def refresh(): Unit = { clearGUI(); if (open) fillGUI() }
  def clearGUI(): Unit = { removeAll(); revalidate() }

  private def fillGUI(): Unit = {
    for (pen <- widget.plot.pens; if (pen.inLegend))
      add(new LegendItem(pen))

    revalidate()
  }

  def setBoldState(state: Int): Unit = {
    boldState = state

    refresh()
  }

  private class LegendItem(pen: PlotPen) extends JPanel with Transparent {
    private val panel = new JPanel {
      setBackground(new Color(pen.color))

      override def getPreferredSize: Dimension =
        new Dimension(widget.zoom(15), widget.zoom(2))

      override def getMaximumSize: Dimension =
        getPreferredSize
    }

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(Box.createHorizontalStrut(10))
    add(panel)
    add(Box.createHorizontalStrut(10))

    add(new JLabel(pen.name) {
      setFont(getFont.deriveFont(boldState))

      override def paintComponent(g: Graphics): Unit = {
        setSize(getWidth.min(LegendItem.this.getWidth - panel.getWidth - 30), getHeight)

        setForeground(InterfaceColors.widgetText())

        super.paintComponent(g)
      }
    })

    add(Box.createHorizontalStrut(10))
  }
}

// FlowLayout wraps its content but doesn't change its vertical size, this custom layout does both (Isaac B 6/15/25)
class WrapLayout extends LayoutManager {
  private val rowGap = 10

  // don't need per-component strings (Isaac B 6/15/25)
  override def addLayoutComponent(name: String, component: Component): Unit = {}

  override def layoutContainer(parent: Container): Unit = {
    parent.getTreeLock synchronized {
      if (parent.getComponentCount == 0)
        return

      val maxWidth = {
        if (parent.getWidth == 0) {
          parent.getMaximumSize.width
        } else {
          parent.getWidth
        }
      }

      val firstSize = parent.getComponent(0).getPreferredSize

      var y = 0
      var rowWidth = firstSize.width
      var rowHeight = firstSize.height
      var row = Seq[Component](parent.getComponent(0))

      def layoutRow(): Unit = {
        var x = (maxWidth / 2 - rowWidth / 2).max(0)

        row.foreach { component =>
          val size = component.getPreferredSize
          val width = (size.width).min(maxWidth)

          component.setBounds(x, y, width, size.height)

          x += width
        }
      }

      parent.getComponents.tail.foreach { component =>
        val size = component.getPreferredSize
        val newWidth = rowWidth + size.width

        if (newWidth > maxWidth) {
          layoutRow()

          y += rowHeight + rowGap
          rowWidth = size.width
          rowHeight = size.height
          row = Seq(component)
        } else {
          rowWidth = newWidth
          rowHeight = rowHeight.max(size.height)
          row = row :+ component
        }
      }

      layoutRow()
    }
  }

  override def minimumLayoutSize(parent: Container): Dimension = {
    parent.getTreeLock synchronized {
      if (parent.getComponentCount == 0)
        return new Dimension(0, 0)

      val maxWidth = {
        if (parent.getWidth == 0) {
          parent.getMaximumSize.width
        } else {
          parent.getWidth
        }
      }

      val firstSize = parent.getComponent(0).getPreferredSize

      var rowWidth = firstSize.width
      var rowHeight = firstSize.height
      var width = 0
      var height = 0

      parent.getComponents.tail.foreach { component =>
        val size = component.getPreferredSize
        val newWidth = rowWidth + size.width

        if (newWidth > maxWidth) {
          width = width.max(rowWidth)

          if (height > 0) {
            height += rowHeight + rowGap
          } else {
            height = rowHeight
          }

          rowWidth = size.width
          rowHeight = size.height
        } else {
          rowWidth = newWidth
          rowHeight = rowHeight.max(size.height)
        }
      }

      if (rowWidth > 0) {
        width = width.max(rowWidth)

        if (height > 0) {
          height += rowHeight + rowGap
        } else {
          height = rowHeight
        }
      }

      new Dimension(width.min(maxWidth), height)
    }
  }

  override def preferredLayoutSize(parent: Container): Dimension =
    minimumLayoutSize(parent)

  // don't need per-component strings (Isaac B 6/15/25)
  override def removeLayoutComponent(component: Component): Unit = {}

}
