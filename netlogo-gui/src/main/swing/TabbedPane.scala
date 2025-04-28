// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Graphics, Insets }
import javax.swing.{ JComponent, JTabbedPane }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TabbedPane extends JTabbedPane with ThemeSync {
  private class TabbedPaneUI extends BasicTabbedPaneUI with ThemeSync {
    override def getTabLabelShiftY(tabPlacement: Int, tabIndex: Int, isSelected: Boolean): Int =
      super.getTabLabelShiftY(tabPlacement, tabIndex, true)

    override def getContentBorderInsets(tabPlacement: Int) =
      new Insets(0, 0, 0, 0)

    override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                    isSelected: Boolean) {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected)
        g2d.setColor(InterfaceColors.tabBackgroundSelected())
      else
        g2d.setColor(InterfaceColors.tabBackground())

      g2d.fillRect(x, y, w, h)
    }

    override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                isSelected: Boolean) {
      // no tab border
    }

    override def paintContentBorder(g: Graphics, tabPlacement: Int, selectedIndex: Int) {
      // no content border
    }

    override def paint(g: Graphics, c: JComponent) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarBackground())
      g2d.fillRect(0, 0, getWidth, getHeight)

      super.paint(g, c)
    }

    override def syncTheme(): Unit = {
      getComponents.foreach(_ match {
        case ts: ThemeSync => ts.syncTheme()
        case _ =>
      })
    }
  }

  private val tabsUI = new TabbedPaneUI

  setUI(tabsUI)
  setFocusable(false)

  addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      setTabForegrounds()
    }
  })

  protected def setTabForegrounds() {
    for (i <- 0 until getTabCount) {
      if (i == getSelectedIndex)
        setForegroundAt(i, InterfaceColors.tabbedPaneTextSelected())
      else
        setForegroundAt(i, InterfaceColors.tabbedPaneText())
    }
  }

  override def syncTheme(): Unit = {
    tabsUI.syncTheme()

    setTabForegrounds()
  }
}
