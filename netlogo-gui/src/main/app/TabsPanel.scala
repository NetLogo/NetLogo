// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Graphics, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.util.function.IntConsumer
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.JTabbedPane
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.app.codetab.{ CodeTab, TemporaryCodeTab }
import org.nlogo.swing.Utils
import org.nlogo.window.InterfaceColors

class TabsPanelUI(tabCount: () => Int, panelWidth: () => Int) extends BasicTabbedPaneUI {
  override def calculateTabHeight(tabPlacement: Int, tabIndex: Int, fontHeight: Int): Int =
    fontHeight + 5
  
  override def getTabLabelShiftY(tabPlacement: Int, tabIndex: Int, isSelected: Boolean): Int =
    super.getTabLabelShiftY(tabPlacement, tabIndex, true)

  override def getTabAreaInsets(tabPlacement: Int): Insets = {
    var x = panelWidth() / 2

    for (i <- 0 until tabCount())
      x -= calculateTabWidth(tabPlacement, i, getFontMetrics) / 2
    
    new Insets(5, x, 5, 0)
  }
  
  override def paintTabArea(g: Graphics, tabPlacement: Int, selectedIndex: Int) {
    super.paintTabArea(g, tabPlacement, selectedIndex)

    val g2d = Utils.initGraphics2D(g)

    var x = getTabAreaInsets(tabPlacement).left + calculateTabWidth(tabPlacement, 0, getFontMetrics)
    val y = getTabAreaInsets(tabPlacement).top
    val height = calculateTabHeight(tabPlacement, 0, getFontMetrics.getHeight)

    g2d.setColor(InterfaceColors.TAB_SEPARATOR)

    for (i <- 1 until tabCount()) {
      if (i != selectedIndex && i != selectedIndex + 1)
        g2d.drawLine(x, y + 5, x, y + height - 5)

      x += calculateTabWidth(tabPlacement, i, getFontMetrics)
    }
  }

  override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                  isSelected: Boolean) {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected)
      g2d.setColor(InterfaceColors.TAB_BACKGROUND_SELECTED)
    else
      g2d.setColor(InterfaceColors.TAB_BACKGROUND)

    if (tabCount() == 1) {
      g2d.fillRoundRect(x, y, w, h, 10, 10)
    }

    else if (tabIndex == 0) {
      g2d.fillRoundRect(x, y, w - 10, h, 10, 10)
      g2d.fillRect(x + w - 20, y, 20, h)
    }

    else if (tabIndex == tabCount() - 1) {
      g2d.fillRoundRect(x + 10, y, w - 10, h, 10, 10)
      g2d.fillRect(x, y, 20, h)
    }

    else {
      g2d.fillRect(x, y, w, h)
    }
  }

  override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                              isSelected: Boolean) {
    val g2d = Utils.initGraphics2D(g)

    if (!isSelected) {
      g2d.setColor(InterfaceColors.TAB_BORDER)

      if (tabIndex == 0) {
        g2d.drawArc(x, y, 10, 10, 90, 90)
        g2d.drawArc(x, y + h - 11, 10, 10, 180, 90)
        g2d.drawLine(x, y + 5, x, y + h - 5)
        g2d.drawLine(x + 5, y, x + w, y)
        g2d.drawLine(x + 5, y + h - 1, x + w, y + h - 1)
      }

      else if (tabIndex == tabCount() - 1) {
        g2d.drawArc(x + w - 10, y, 10, 10, 0, 90)
        g2d.drawArc(x + w - 10, y + h - 11, 10, 10, 270, 90)
        g2d.drawLine(x + w, y + 5, x + w, y + h - 5)
        g2d.drawLine(x, y, x + w - 5, y)
        g2d.drawLine(x, y + h - 1, x + w - 5, y + h - 1)
      }

      else {
        g2d.drawLine(x, y, x + w, y)
        g2d.drawLine(x, y + h - 1, x + w, y + h - 1)
      }
    }
  }

  override def paintContentBorder(g: Graphics, tabPlacement: Int, selectedIndex: Int) {
    // no content border
  }
}

class TabsPanel(val tabManager: TabManager) extends JTabbedPane with ChangeListener {
  putClientProperty("JTabbedPane.tabCloseCallback", new IntConsumer {
    def accept(index: Int) {
      val tab = getComponentAt(index).asInstanceOf[TemporaryCodeTab]

      tab.prepareForClose()
      tabManager.closeExternalTab(tab)
    }
  })

  setUI(new TabsPanelUI(() => getTabCount, () => getWidth))
  setFocusable(false)

  addChangeListener(this)

  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) {
      if (e.getClickCount == 1 && e.isControlDown && tabManager.getSelectedTab.isInstanceOf[CodeTab]) {
        if (tabManager.separateTabsWindow.isAncestorOf(tabManager.getSelectedTab))
          tabManager.switchWindow(false, true)
        else
          tabManager.switchWindow(true)
      }
    }
  })

  def stateChanged(e: ChangeEvent) =
    tabManager.switchedTabs(getSelectedComponent)
  
  def focusSelected() =
    getSelectedComponent.requestFocus
}
