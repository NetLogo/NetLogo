// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets, Point }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.{ JComponent, JLabel, JPanel, JTabbedPane, SwingConstants }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ CloseButton, Utils }
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

  override def paintTabArea(g: Graphics, tabPlacement: Int, selectedIndex: Int): Unit = {
    super.paintTabArea(g, tabPlacement, selectedIndex)

    val g2d = Utils.initGraphics2D(g)

    var x = getTabAreaInsets(tabPlacement).left + calculateTabWidth(tabPlacement, 0, getFontMetrics)
    val y = getTabAreaInsets(tabPlacement).top
    val height = calculateTabHeight(tabPlacement, 0, getFontMetrics.getHeight)

    g2d.setColor(InterfaceColors.tabSeparator())

    for (i <- 1 until tabsPanel.getTabCount) {
      if (i != selectedIndex && i != selectedIndex + 1)
        g2d.drawLine(x, y + 5, x, y + height - 5)

      x += calculateTabWidth(tabPlacement, i, getFontMetrics)
    }
  }

  override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                  isSelected: Boolean): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected) {
      if (tabsPanel.getError(tabIndex)) {
        g2d.setColor(InterfaceColors.tabBackgroundError())
      } else {
        g2d.setColor(InterfaceColors.tabBackgroundSelected())
      }
    } else if (tabsPanel.isHover(tabIndex)) {
      g2d.setColor(InterfaceColors.tabBackgroundHover())
    } else {
      g2d.setColor(InterfaceColors.tabBackground())
    }

    if (tabsPanel.getTabCount == 1) {
      g2d.fillRoundRect(x, y, w, h, 10, 10)
    } else if (tabIndex == 0) {
      g2d.fillRoundRect(x, y, w - 10, h, 10, 10)
      g2d.fillRect(x + w - 20, y, 20, h)
    } else if (tabIndex == tabsPanel.getTabCount - 1) {
      g2d.fillRoundRect(x + 10, y, w - 10, h, 10, 10)
      g2d.fillRect(x, y, 20, h)
    } else {
      g2d.fillRect(x, y, w, h)
    }
  }

  override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                              isSelected: Boolean): Unit = {
    if (!isSelected) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.tabBorder())

      if (tabIndex == 0) {
        g2d.drawArc(x, y, 10, 10, 90, 90)
        g2d.drawArc(x, y + h - 11, 10, 10, 180, 90)
        g2d.drawLine(x, y + 5, x, y + h - 5)
        g2d.drawLine(x + 5, y, x + w, y)
        g2d.drawLine(x + 5, y + h - 1, x + w, y + h - 1)
      } else if (tabIndex == tabsPanel.getTabCount - 1) {
        g2d.drawArc(x + w - 10, y, 10, 10, 0, 90)
        g2d.drawArc(x + w - 10, y + h - 11, 10, 10, 270, 90)
        g2d.drawLine(x + w, y + 5, x + w, y + h - 5)
        g2d.drawLine(x, y, x + w - 5, y)
        g2d.drawLine(x, y + h - 1, x + w - 5, y + h - 1)
      } else {
        g2d.drawLine(x, y, x + w, y)
        g2d.drawLine(x, y + h - 1, x + w, y + h - 1)
      }
    }
  }

  override def paintContentBorder(g: Graphics, tabPlacement: Int, selectedIndex: Int): Unit = {
    // don't draw default content border (Isaac B 2/9/25)
  }

  override def paint(g: Graphics, c: JComponent): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.toolbarBackground())
    g2d.fillRect(0, 0, tabsPanel.getWidth, tabsPanel.getHeight)

    super.paint(g, c)
  }
}

class TabLabel(startPanel: TabsPanel, text: String, tab: Component) extends JPanel(new GridBagLayout) {
  private var tabsPanel: TabsPanel = startPanel

  def setTabsPanel(tabsPanel: TabsPanel): Unit = {
    this.tabsPanel = tabsPanel
  }

  private val textLabel = new JLabel(text)

  private var rawText = text

  def setText(text: String): Unit = {
    rawText = text
  }

  def getText: String =
    rawText

  def boldWidth: Int =
    new JLabel(s"<html><b>$rawText</b></html>").getPreferredSize.width

  private var closeButton: Option[CloseButton] = None

  var error = false

  locally {
    setOpaque(false)

    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    add(textLabel, c)

    tab match {
      case main: MainCodeTab =>

      case codeTab: CodeTab =>
        val button = new CloseButton

        button.addMouseListener(new MouseAdapter {
          override def mouseClicked(e: MouseEvent): Unit = {
            if (e.getButton == MouseEvent.BUTTON1) {
              try {
                codeTab.close()
              } catch {
                case e: UserCancelException =>
              }
            }
          }
        })

        c.fill = GridBagConstraints.NONE
        c.weightx = 0

        add(button, c)

        closeButton = Some(button)

      case _ =>
    }
  }

  override def getPreferredSize: Dimension =
    new Dimension(boldWidth + closeButton.map(_.getPreferredSize.width + 10).getOrElse(0),
                  super.getPreferredSize.height)

  override def paintComponent(g: Graphics): Unit = {
    if (tab == tabsPanel.getSelectedComponent) {
      textLabel.setForeground(InterfaceColors.tabTextSelected())
      textLabel.setText("<html><b>" + rawText + "</b></html>")

      closeButton.foreach(_.setForeground(InterfaceColors.tabTextSelected()))
    } else if (tabsPanel.getError(tabsPanel.indexOfComponent(tab))) {
      textLabel.setForeground(InterfaceColors.tabTextError())
      textLabel.setText("<html><b>" + rawText + "</b></html>")

      closeButton.foreach(_.setForeground(InterfaceColors.tabTextError()))
    } else {
      textLabel.setForeground(InterfaceColors.tabText())
      textLabel.setText(rawText)

      closeButton.foreach(_.setForeground(InterfaceColors.tabText()))
    }

    super.paintComponent(g)
  }
}

class TabsPanel(val tabManager: TabManager) extends JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
                                            with ChangeListener {

  private var mouse: Option[Point] = None

  setUI(new TabsPanelUI(this))
  setFocusable(false)

  addChangeListener(this)

  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount == 1 && e.isControlDown && tabManager.getSelectedTab.isInstanceOf[CodeTab]) {
        if (tabManager.separateTabsWindow.isAncestorOf(tabManager.getSelectedTab)) {
          tabManager.switchWindow(false, true)
        } else {
          tabManager.switchWindow(true)
        }
      }
    }

    override def mouseExited(e: MouseEvent): Unit = {
      mouse = None

      repaint()
    }
  })

  addMouseMotionListener(new MouseMotionAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      mouse = Option(e.getPoint)

      repaint()
    }
  })

  def addTabWithLabel(tab: Component, label: TabLabel): Unit = {
    addTab(null, tab)
    setTabComponentAt(getTabCount - 1, label)

    if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {
      setToolTipTextAt(getTabCount - 1, s"Cmd+${tabManager.getTotalTabIndex(tab) + 1}")
    } else {
      setToolTipTextAt(getTabCount - 1, s"Ctrl+${tabManager.getTotalTabIndex(tab) + 1}")
    }
  }

  def stateChanged(e: ChangeEvent): Unit =
    tabManager.switchedTabs(getSelectedComponent)

  def focusSelected(): Unit =
    getSelectedComponent.requestFocus

  def getError(index: Int): Boolean =
    getTabLabelAt(index).error

  def setError(index: Int, error: Boolean): Unit = {
    getTabLabelAt(index).error = error
  }

  def getTabLabelAt(index: Int): TabLabel =
    getTabComponentAt(index).asInstanceOf[TabLabel]

  def isHover(index: Int): Boolean = {
    mouse match {
      case Some(point) if getTabCount > 0 =>
        val component = getTabComponentAt(index)

        component.getX - 10 <= point.x && component.getX + 10 + component.getWidth >= point.x &&
        component.getY <= point.y && component.getY + component.getHeight >= point.y

      case _ => false
    }
  }
}
