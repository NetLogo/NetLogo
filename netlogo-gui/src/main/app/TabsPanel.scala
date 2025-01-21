// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Dimension, FlowLayout, Graphics, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.{ Box, JComponent, JLabel, JPanel, JTabbedPane, SwingConstants }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ CloseButton, HoverDecoration, Utils }
import org.nlogo.theme.InterfaceColors

private class TabsPanelUI(tabsPanel: TabsPanel) extends BasicTabbedPaneUI {
  override def getContentBorderInsets(tabPlacement: Int) =
    new Insets(0, 0, 0, 0)

  override def calculateTabHeight(tabPlacement: Int, tabIndex: Int, fontHeight: Int): Int =
    fontHeight + 5

  override def getTabLabelShiftY(tabPlacement: Int, tabIndex: Int, isSelected: Boolean): Int =
    super.getTabLabelShiftY(tabPlacement, tabIndex, true)

  override def getTabAreaInsets(tabPlacement: Int): Insets = {
    var x = tabsPanel.getWidth / 2

    for (i <- 0 until tabsPanel.getTabCount)
      x -= calculateTabWidth(tabPlacement, i, getFontMetrics) / 2

    new Insets(10, x, 0, 0)
  }

  override def paintTabArea(g: Graphics, tabPlacement: Int, selectedIndex: Int) {
    super.paintTabArea(g, tabPlacement, selectedIndex)

    val g2d = Utils.initGraphics2D(g)

    var x = getTabAreaInsets(tabPlacement).left + calculateTabWidth(tabPlacement, 0, getFontMetrics)
    val y = getTabAreaInsets(tabPlacement).top
    val height = calculateTabHeight(tabPlacement, 0, getFontMetrics.getHeight)

    g2d.setColor(InterfaceColors.TAB_SEPARATOR)

    for (i <- 1 until tabsPanel.getTabCount) {
      if (i != selectedIndex && i != selectedIndex + 1)
        g2d.drawLine(x, y + 5, x, y + height - 5)

      x += calculateTabWidth(tabPlacement, i, getFontMetrics)
    }
  }

  override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                  isSelected: Boolean) {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected) {
      if (tabsPanel.getError(tabIndex))
        g2d.setColor(InterfaceColors.TAB_BACKGROUND_ERROR)
      else
        g2d.setColor(InterfaceColors.TAB_BACKGROUND_SELECTED)
    }

    else
      g2d.setColor(InterfaceColors.TAB_BACKGROUND)

    if (tabsPanel.getTabCount == 1) {
      g2d.fillRoundRect(x, y, w, h, 10, 10)
    }

    else if (tabIndex == 0) {
      g2d.fillRoundRect(x, y, w - 10, h, 10, 10)
      g2d.fillRect(x + w - 20, y, 20, h)
    }

    else if (tabIndex == tabsPanel.getTabCount - 1) {
      g2d.fillRoundRect(x + 10, y, w - 10, h, 10, 10)
      g2d.fillRect(x, y, 20, h)
    }

    else {
      g2d.fillRect(x, y, w, h)
    }
  }

  override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                              isSelected: Boolean) {
    if (!isSelected) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.TAB_BORDER)

      if (tabIndex == 0) {
        g2d.drawArc(x, y, 10, 10, 90, 90)
        g2d.drawArc(x, y + h - 11, 10, 10, 180, 90)
        g2d.drawLine(x, y + 5, x, y + h - 5)
        g2d.drawLine(x + 5, y, x + w, y)
        g2d.drawLine(x + 5, y + h - 1, x + w, y + h - 1)
      }

      else if (tabIndex == tabsPanel.getTabCount - 1) {
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

  override def paint(g: Graphics, c: JComponent) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.TOOLBAR_BACKGROUND)
    g2d.fillRect(0, 0, tabsPanel.getWidth, tabsPanel.getHeight)

    super.paint(g, c)
  }
}

class TabLabel(text: String, tab: Component) extends JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
  private var tabsPanel: TabsPanel = null

  def setTabsPanel(tabsPanel: TabsPanel) {
    this.tabsPanel = tabsPanel
  }

  private val textLabel = new JLabel(text)

  private var rawText = text

  def setText(text: String) {
    rawText = text
  }

  def getText: String =
    rawText

  def boldWidth: Int =
    new JLabel("<html><b>" + rawText + "</b></html>").getPreferredSize.width

  private var closeButton: CloseButton = null

  var error = false

  setOpaque(false)

  add(textLabel)

  tab match {
    case main: MainCodeTab =>

    case codeTab: CodeTab =>
      closeButton = new CloseButton

      closeButton.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent) {
          if (e.getButton == MouseEvent.BUTTON1) {
            try {
              codeTab.close()
            }

            catch {
              case e: UserCancelException =>
            }
          }
        }
      })

      add(Box.createHorizontalStrut(10))
      add(closeButton)

    case _ =>
  }

  override def getPreferredSize: Dimension =
    new Dimension(boldWidth, super.getPreferredSize.height)

  override def paintComponent(g: Graphics) {
    if (tab == tabsPanel.getSelectedComponent) {
      textLabel.setForeground(InterfaceColors.TAB_TEXT_SELECTED)
      textLabel.setText("<html><b>" + rawText + "</b></html>")

      if (closeButton != null)
        closeButton.setForeground(InterfaceColors.TAB_TEXT_SELECTED)
    }

    else if (tabsPanel.getError(tabsPanel.indexOfComponent(tab))) {
      textLabel.setForeground(InterfaceColors.TAB_TEXT_ERROR)
      textLabel.setText("<html><b>" + rawText + "</b></html>")

      if (closeButton != null)
        closeButton.setForeground(InterfaceColors.TAB_TEXT_ERROR)
    }

    else {
      textLabel.setForeground(InterfaceColors.TAB_TEXT)
      textLabel.setText(rawText)

      if (closeButton != null)
        closeButton.setForeground(InterfaceColors.TAB_TEXT)
    }

    super.paintComponent(g)
  }
}

class TabsPanel(val tabManager: TabManager) extends JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
                                            with ChangeListener with HoverDecoration {
  setUI(new TabsPanelUI(this))
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

  def getError(index: Int): Boolean =
    getTabLabelAt(index).error

  def setError(index: Int, error: Boolean) {
    getTabLabelAt(index).error = error
  }

  def getTabLabelAt(index: Int): TabLabel =
    getTabComponentAt(index).asInstanceOf[TabLabel]
}
