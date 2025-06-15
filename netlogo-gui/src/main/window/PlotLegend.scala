// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Container, Dimension, FlowLayout, Font, Graphics, LayoutManager }
import javax.swing.{ JLabel, JPanel }

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

  private class LegendItem(pen: PlotPen) extends JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) with Transparent {
    add(new JPanel {
      setBackground(new Color(pen.color))

      override def getPreferredSize: Dimension =
        new Dimension(widget.zoom(15), widget.zoom(2))
    })

    add(new JLabel(pen.name) {
      setFont(getFont.deriveFont(boldState))

      override def paintComponent(g: Graphics): Unit = {
        setForeground(InterfaceColors.widgetText())

        super.paintComponent(g)
      }
    })
  }
}

// FlowLayout wraps its content but doesn't change its vertical size, this custom layout does both (Isaac B 6/15/25)
class WrapLayout extends LayoutManager {
  private val rowGap = 10

  // don't need per-component strings (Isaac B 6/15/25)
  override def addLayoutComponent(name: String, component: Component): Unit = {}

  override def layoutContainer(parent: Container): Unit = {
    parent.getTreeLock synchronized {
      val maxWidth = {
        if (parent.getWidth == 0) {
          parent.getMaximumSize.width
        } else {
          parent.getWidth
        }
      }

      var y = 0
      var rowWidth = 0
      var rowHeight = 0
      var row = Seq[Component]()

      def layoutRow(): Unit = {
        var x = maxWidth / 2 - rowWidth / 2

        row.foreach { component =>
          val size = component.getPreferredSize

          component.setBounds(x, y, size.width, size.height)

          x += size.width
        }
      }

      parent.getComponents.foreach { component =>
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
      val maxWidth = {
        if (parent.getWidth == 0) {
          parent.getMaximumSize.width
        } else {
          parent.getWidth
        }
      }

      var rowWidth = 0
      var rowHeight = 0
      var width = 0
      var height = 0

      parent.getComponents.foreach { component =>
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

      new Dimension(width, height)
    }
  }

  override def preferredLayoutSize(parent: Container): Dimension =
    minimumLayoutSize(parent)

  // don't need per-component strings (Isaac B 6/15/25)
  override def removeLayoutComponent(component: Component): Unit = {}

}
