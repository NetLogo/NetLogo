// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Graphics }
import java.awt.event.{ ActionEvent, MouseEvent }
import javax.swing.{ AbstractAction, JButton }
import javax.swing.border.EmptyBorder

import org.nlogo.app.codetab.CodeTab
import org.nlogo.awt.Mouse
import org.nlogo.swing.{ FloatingTabbedPane, MouseUtils, TabLabel, Utils }
import org.nlogo.theme.InterfaceColors

class TabsPanel(val tabManager: TabManager) extends FloatingTabbedPane {
  private val addButton = new JButton(new AbstractAction("+") {
    override def actionPerformed(e: ActionEvent): Unit = {
      tabManager.newExternalFile()
    }
  }) with MouseUtils {
    setBackground(InterfaceColors.Transparent)
    setBorder(new EmptyBorder(3, 3, 3, 3))

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isPressed) {
        setForeground(InterfaceColors.tabTextSelected())
        g2d.setColor(InterfaceColors.tabBackgroundSelected())
      } else if (isHover) {
        setForeground(InterfaceColors.tabText())
        g2d.setColor(InterfaceColors.tabBackgroundHover())
      } else {
        setForeground(InterfaceColors.tabText())
        g2d.setColor(InterfaceColors.tabBackground())
      }

      g2d.fillRoundRect(0, 0, getWidth, getHeight, 10, 10)
      g2d.fillRect(0, 0, getWidth / 2, getHeight)

      g2d.setColor(InterfaceColors.tabBorder())
      g2d.drawArc(getWidth - 10, 0, 10, 10, 0, 90)
      g2d.drawArc(getWidth - 10, getHeight - 11, 10, 10, 270, 90)
      g2d.drawLine(getWidth, 5, getWidth, getHeight - 5)
      g2d.drawLine(0, 0, getWidth - 5, 0)
      g2d.drawLine(0, getHeight - 1, getWidth - 5, getHeight - 1)

      super.paintComponent(g)
    }
  }

  tabArea.add(addButton)

  override def addTabWithLabel(tab: Component, label: TabLabel): Unit = {
    super.addTabWithLabel(tab, label)

    if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {
      setToolTipTextAt(getTabCount - 1, s"Cmd+${tabManager.getTotalTabIndex(tab) + 1}")
    } else {
      setToolTipTextAt(getTabCount - 1, s"Ctrl+${tabManager.getTotalTabIndex(tab) + 1}")
    }
  }

  override def interceptTabClick(tab: Component, e: MouseEvent): Unit = {
    if (Mouse.hasCtrl(e) && e.getClickCount == 1 && tab.isInstanceOf[CodeTab]) {
      if (tabManager.separateTabsWindow.isAncestorOf(tab)) {
        tabManager.switchWindow(false, true)
      } else {
        tabManager.switchWindow(true)
      }
    } else {
      setSelectedComponent(tab)
    }
  }

  override def setSelectedComponent(component: Component): Unit = {
    super.setSelectedComponent(component)

    tabManager.switchedTabs(component)
  }

  def setAddButtonVisible(visible: Boolean): Unit = {
    addButton.setVisible(visible)

    revalidate()
    repaint()
  }
}
