// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Container, Dimension, LayoutManager, Toolkit }

// This is for working around the Swing bug where if you have lots and lots
// of items in your menu they don't scroll or split into multiple columns.
// Here we force splitting into multiple columns.
// This was inspired by cpol's 4/28/00 post to
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4246124 .

class WrappingPopupMenu extends PopupMenu {
  override def show(invoker: Component, x: Int, y: Int) {
    setLayout(new WrappingLayout((Toolkit.getDefaultToolkit.getScreenSize.getHeight
                                  * 0.7 / getFontMetrics(getFont).getHeight).toInt))

    super.show(invoker, x, y)
  }

  private class WrappingLayout(rows: Int) extends LayoutManager {
    // not implemented
    def addLayoutComponent(name: String, comp: Component) {}

    // not implemented
    def removeLayoutComponent(comp: Component) {}

    def layoutContainer(target: Container) {
      val insets = target.getInsets
      var x = 0;
      var y = insets.top
      var columnWidth = 0
      var lastRowStart = 0

      for (i <- 0 until target.getComponentCount) {
        if (i % rows == 0) {
          for (j <- lastRowStart until i)
            target.getComponent(j).setSize(columnWidth, target.getComponent(j).getHeight)
          
          lastRowStart = i
          x += columnWidth + insets.left
          columnWidth = 0
          y = insets.top
        }

        val comp = target.getComponent(i)
        val pref = comp.getPreferredSize

        comp.setBounds(x, y, pref.width, pref.height)

        if (pref.width > columnWidth)
          columnWidth = pref.width
        
        y += pref.height
      }

      for (j <- lastRowStart until target.getComponentCount)
        target.getComponent(j).setSize(columnWidth, target.getComponent(j).getHeight)
    }

    def minimumLayoutSize(target: Container): Dimension = {
      val insets = target.getInsets
      var x = 0;
      var columnWidth = 0
      var columnHeight = 0
      var maxColumnHeight = 0

      for (i <- 0 until target.getComponentCount) {
        if (i % rows == 0) {
          x += columnWidth + insets.left

          if (columnHeight > maxColumnHeight)
            maxColumnHeight = columnHeight
          
          columnWidth = 0
          columnHeight = insets.top + insets.bottom
        }

        val comp = target.getComponent(i)
        val pref = comp.getPreferredSize

        columnHeight += pref.height

        if (pref.width > columnWidth)
          columnWidth = pref.width
      }

      if (columnHeight > maxColumnHeight)
        maxColumnHeight = columnHeight
      
      new Dimension(x + columnWidth + insets.right, maxColumnHeight)
    }

    def preferredLayoutSize(target: Container): Dimension =
      minimumLayoutSize(target)
  }
}
