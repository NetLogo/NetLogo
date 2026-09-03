// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, FontMetrics, Graphics, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.{ JComponent, JLabel, JTabbedPane, SwingConstants }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.core.I18N
import org.nlogo.theme.InterfaceColors

import scala.util.Try

private class FloatingTabbedPaneUI(tabbedPane: FloatingTabbedPane) extends BasicTabbedPaneUI {
  override def getContentBorderInsets(tabPlacement: Int) =
    new Insets(0, 0, 0, 0)

  override def calculateTabWidth(tabPlacement: Int, tabIndex: Int, metrics: FontMetrics): Int = {
    val tabWidth: Int = super.calculateTabWidth(tabPlacement, tabIndex, metrics)
    val labelWidth: Int = tabbedPane.getTabLabelAt(tabIndex).fold(0)(_.getPreferredSize.width)

    labelWidth + Utils.zoom(tabWidth - labelWidth)
  }

  def calculateTabWidth(tabPlacement: Int, tabIndex: Int): Int =
    calculateTabWidth(tabPlacement, tabIndex, getFontMetrics)

  override def calculateTabHeight(tabPlacement: Int, tabIndex: Int, fontHeight: Int): Int =
    fontHeight + Utils.zoom(5)

  def calculateTabHeight(tabPlacement: Int, tabIndex: Int): Int =
    calculateTabHeight(tabPlacement, tabIndex, getFontMetrics.getHeight)

  override def getTabLabelShiftY(tabPlacement: Int, tabIndex: Int, isSelected: Boolean): Int =
    super.getTabLabelShiftY(tabPlacement, tabIndex, true)

  override def getTabAreaInsets(tabPlacement: Int): Insets = {
    var x = tabbedPane.getWidth / 2

    for (i <- 0 until tabbedPane.getTabCount)
      x -= calculateTabWidth(tabPlacement, i) / 2

    new Insets(Utils.zoom(10), x, 0, 0)
  }

  override def paintTabArea(g: Graphics, tabPlacement: Int, selectedIndex: Int): Unit = {
    super.paintTabArea(g, tabPlacement, selectedIndex)

    val g2d = Utils.initGraphics2D(g)

    var x = getTabAreaInsets(tabPlacement).left + calculateTabWidth(tabPlacement, 0)
    val y = getTabAreaInsets(tabPlacement).top
    val height = calculateTabHeight(tabPlacement, 0)

    g2d.setColor(InterfaceColors.tabSeparator())

    for (i <- 1 until tabbedPane.getTabCount) {
      if (i != selectedIndex && i != selectedIndex + 1)
        g2d.drawLine(x, y + Utils.zoom(5), x, y + height - Utils.zoom(5))

      x += calculateTabWidth(tabPlacement, i)
    }
  }

  override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                  isSelected: Boolean): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected) {
      if (tabbedPane.getError(tabIndex)) {
        g2d.setColor(InterfaceColors.tabBackgroundError())
      } else {
        g2d.setColor(InterfaceColors.tabBackgroundSelected())
      }
    } else if (tabbedPane.isHover(tabIndex)) {
      g2d.setColor(InterfaceColors.tabBackgroundHover())
    } else {
      g2d.setColor(InterfaceColors.tabBackground())
    }

    val diameter: Int = Utils.zoom(10)

    if (tabbedPane.getTabCount == 1) {
      g2d.fillRoundRect(x, y, w, h, diameter, diameter)
    } else if (tabIndex == 0) {
      g2d.fillRoundRect(x, y, w - diameter, h, diameter, diameter)
      g2d.fillRect(x + w - diameter * 2, y, diameter * 2, h)
    } else if (tabIndex == tabbedPane.getTabCount - 1) {
      g2d.fillRoundRect(x + diameter, y, w - diameter, h, diameter, diameter)
      g2d.fillRect(x, y, diameter * 2, h)
    } else {
      g2d.fillRect(x, y, w, h)
    }
  }

  override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                              isSelected: Boolean): Unit = {
    if (!isSelected) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.tabBorder())

      val diameter: Int = Utils.zoom(10)
      val radius: Int = diameter / 2

      if (tabIndex == 0) {
        g2d.drawArc(x, y, diameter, diameter, 90, 90)
        g2d.drawArc(x, y + h - diameter - 1, diameter, diameter, 180, 90)
        g2d.drawLine(x, y + radius, x, y + h - radius)
        g2d.drawLine(x + radius, y, x + w, y)
        g2d.drawLine(x + radius, y + h - 1, x + w, y + h - 1)
      } else if (tabIndex == tabbedPane.getTabCount - 1) {
        g2d.drawArc(x + w - diameter, y, diameter, diameter, 0, 90)
        g2d.drawArc(x + w - diameter, y + h - diameter - 1, diameter, diameter, 270, 90)
        g2d.drawLine(x + w, y + radius, x + w, y + h - radius)
        g2d.drawLine(x, y, x + w - radius, y)
        g2d.drawLine(x, y + h - 1, x + w - radius, y + h - 1)
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

    g2d.setColor(tabbedPane.getBackground)
    g2d.fillRect(0, 0, tabbedPane.getWidth, tabbedPane.getHeight)

    super.paint(g, c)
  }
}

// allows automatic adding of close button to arbitrary tabs (Isaac B 5/21/25)
trait CloseableTab extends Component {
  def close(): Unit
}

trait RenameableTab {
  def rename(): Unit
}

class TabLabel(startPane: FloatingTabbedPane, text: String, tab: Component) extends BoxRow(10) {
  private var tabbedPane: FloatingTabbedPane = startPane

  def setTabbedPane(tabbedPane: FloatingTabbedPane): Unit = {
    this.tabbedPane = tabbedPane
  }

  private val textLabel = new JLabel(text) with Zoomable

  private var rawText = text

  def setText(text: String): Unit = {
    rawText = text

    repaint()
  }

  def getText: String =
    rawText

  private def boldWidth: Int = {
    new JLabel(s"<html><b>$rawText</b></html>") with Zoomable {
      zoom()
    }.getPreferredSize.width
  }

  private var closeButton: Option[CloseButton] = None
  private var popupMenu: Option[PopupMenu] = None

  var error = false

  add(textLabel)

  tab match {
    case closeable: CloseableTab =>
      val button = new CloseButton

      button.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent): Unit = {
          if (e.getButton == MouseEvent.BUTTON1)
            closeable.close()
        }
      })

      add(button)

      closeButton = Some(button)

    case _ =>
  }

  tab match {
    case temp: RenameableTab =>
      popupMenu = Some(new PopupMenu {
        add(new MenuItem(I18N.gui.get("tabs.external.rename"), () => temp.rename()))
      })

    case _ =>
  }

  override def contains(x: Int, y: Int): Boolean =
    closeButton.exists(button => button.contains(x - button.getX, y - button.getY))

  override def getPreferredSize: Dimension =
    new Dimension(boldWidth + closeButton.fold(0)(_.getPreferredSize.width + Utils.zoom(10)),
                  super.getPreferredSize.height)

  override def paintComponent(g: Graphics): Unit = {
    if (tab == tabbedPane.getSelectedComponent) {
      textLabel.setForeground(InterfaceColors.tabTextSelected())
      textLabel.setText("<html><b>" + rawText + "</b></html>")

      closeButton.foreach(_.setForeground(InterfaceColors.tabTextSelected()))
    } else if (tabbedPane.getError(tabbedPane.indexOfComponent(tab))) {
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

  def showPopup(): Unit = {
    popupMenu.foreach { popup =>
      popup.syncTheme()
      popup.show(this, 0, getHeight)
    }
  }
}

class FloatingTabbedPane extends JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) with Zoomable {
  private val tabUI = new FloatingTabbedPaneUI(this)
  private var mouse: Option[Int] = None

  setUI(tabUI)
  setFocusable(false)

  addMouseListener(new MouseAdapter {
    override def mouseExited(e: MouseEvent): Unit = {
      if (mouse.isDefined) {
        mouse = None

        repaint()
      }
    }
  })

  addMouseMotionListener(new MouseMotionAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      val insets: Insets = tabUI.getTabAreaInsets(SwingConstants.TOP)

      var x: Int = insets.left
      val y: Int = insets.top

      (0 until getTabCount).find { i =>
        val tabWidth: Int = tabUI.calculateTabWidth(SwingConstants.TOP, i)
        val tabHeight: Int = tabUI.calculateTabHeight(SwingConstants.TOP, i)

        val result: Boolean = x <= e.getX && x + tabWidth >= e.getX && y <= e.getY && y + tabHeight >= e.getY

        x += tabWidth

        result
      } match {
        case Some(i) =>
          if (!mouse.exists(_ == i)) {
            mouse = Option(i)

            repaint()
          }

        case None =>
          if (mouse.isDefined) {
            mouse = None

            repaint()
          }
      }
    }
  })

  override def processMouseEvent(e: MouseEvent): Unit = {
    if (e.getButton == MouseEvent.BUTTON3) {
      mouse.flatMap(getTabLabelAt) match {
        case Some(label) =>
          label.showPopup()

        case _ =>
          super.processMouseEvent(e)
      }
    } else {
      super.processMouseEvent(e)
    }
  }

  def addTabWithLabel(tab: Component, label: TabLabel): Unit = {
    addTab(null, tab)
    setTabComponentAt(getTabCount - 1, label)
  }

  def focusSelected(): Unit =
    getSelectedComponent.requestFocus

  def getTabLabelAt(index: Int): Option[TabLabel] =
    Try(getTabComponentAt(index)).map(c => Option(c.asInstanceOf[TabLabel])).getOrElse(None)

  def getError(index: Int): Boolean =
    getTabLabelAt(index).fold(false)(_.error)

  def setError(index: Int, error: Boolean): Unit = {
    getTabLabelAt(index).foreach(_.error = error)
  }

  def isHover(index: Int): Boolean =
    mouse.contains(index)
}
