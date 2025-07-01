// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Component, Dimension, FlowLayout, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JComponent, JLabel, JPanel, ScrollPaneConstants }

import org.nlogo.swing.ScrollPane
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class FloatingTabbedPane extends JPanel(new BorderLayout) with Transparent with ThemeSync {
  protected val tabArea = new TabArea(this)
  protected val scrollPane = new ScrollPane(tabArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
  protected val contentArea = new CardPanel

  scrollPane.setBorder(null)

  add(scrollPane, BorderLayout.NORTH)
  add(contentArea, BorderLayout.CENTER)

  def getTabCount: Int =
    tabArea.getComponents.count(_.isInstanceOf[TabLabel])

  def indexOfComponent(component: Component): Int =
    contentArea.getComponents.indexOf(component)

  def getTabLabelAt(index: Int): TabLabel =
    tabArea.getComponent(index).asInstanceOf[TabLabel]

  def getComponentAt(index: Int): Component =
    contentArea.getComponent(index)

  def setToolTipTextAt(index: Int, text: String): Unit = {
    tabArea.getComponent(index) match {
      case component: JComponent => component.setToolTipText(text)
      case _ =>
    }
  }

  def getSelectedComponent: Component =
    contentArea.getShown

  def setSelectedComponent(component: Component): Unit = {
    contentArea.show(component)
    tabArea.repaint()
  }

  def getSelectedIndex: Int =
    contentArea.getComponents.indexOf(contentArea.getShown)

  def setSelectedIndex(index: Int): Unit = {
    if (index >= 0 && index < contentArea.getComponentCount) {
      setSelectedComponent(contentArea.getComponent(index))
    }
  }

  def setComponentAt(index: Int, component: Component): Unit = {
    if (index >= 0 && index < contentArea.getComponentCount) {
      contentArea.remove(index)
      contentArea.add(component, index)
    }
  }

  override def remove(component: Component): Unit = {
    val index = indexOfComponent(component)

    if (index != -1) {
      tabArea.remove(tabArea.getComponent(index))
      contentArea.remove(component)

      revalidate()
      repaint()
    }
  }

  def getError(index: Int): Boolean =
    index >= 0 && index < tabArea.getComponentCount && getTabLabelAt(index).error

  def setError(index: Int, error: Boolean): Unit = {
    if (index >= 0 && index < tabArea.getComponentCount)
      getTabLabelAt(index).error = error
  }

  def focusSelected(): Unit = {
    Option(getSelectedComponent).foreach(_.requestFocus())
  }

  def addTabWithLabel(tab: Component, label: TabLabel): Unit = {
    tabArea.add(label, tabArea.getComponents.lastIndexWhere(_.isInstanceOf[TabLabel]) + 1)
    contentArea.add(tab)
  }

  def interceptTabClick(tab: Component, e: MouseEvent): Unit = {
    setSelectedComponent(tab)
  }

  override def syncTheme(): Unit = {
    scrollPane.setBackground(InterfaceColors.toolbarBackground())

    tabArea.getComponents.foreach {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }

    contentArea.getComponents.foreach {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }
  }
}

class TabArea(tabbedPane: FloatingTabbedPane)
  extends JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10)) with Transparent {

  override def getComponentCount: Int =
    getComponents.count(_.isVisible)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    var index = 0

    getComponents.foreach {
      case component: TabLabel =>
        if (index == tabbedPane.getSelectedIndex) {
          if (tabbedPane.getError(index)) {
            g2d.setColor(InterfaceColors.tabBackgroundError())
          } else {
            g2d.setColor(InterfaceColors.tabBackgroundSelected())
          }
        } else if (tabbedPane.getTabLabelAt(index).isHover) {
          g2d.setColor(InterfaceColors.tabBackgroundHover())
        } else {
          g2d.setColor(InterfaceColors.tabBackground())
        }

        val x = component.getX
        val y = component.getY
        val w = component.getWidth
        val h = component.getHeight

        if (index == 0) {
          g2d.fillRoundRect(x, y, w - 10, h, 10, 10)
          g2d.fillRect(x + w - 20, y, 20, h)
        } else if (index == getComponentCount - 1) {
          g2d.fillRoundRect(x + 10, y, w - 10, h, 10, 10)
          g2d.fillRect(x, y, 20, h)
        } else {
          g2d.fillRect(x, y, w, h)
        }

        if (index != tabbedPane.getSelectedIndex) {
          g2d.setColor(InterfaceColors.tabBorder())

          if (index == 0) {
            g2d.drawArc(x, y, 10, 10, 90, 90)
            g2d.drawArc(x, y + h - 11, 10, 10, 180, 90)
            g2d.drawLine(x, y + 5, x, y + h - 5)
            g2d.drawLine(x + 5, y, x + w, y)
            g2d.drawLine(x + 5, y + h - 1, x + w, y + h - 1)
          } else if (index == getComponentCount - 1) {
            g2d.drawArc(x + w - 10, y, 10, 10, 0, 90)
            g2d.drawArc(x + w - 10, y + h - 11, 10, 10, 270, 90)
            g2d.drawLine(x + w, y + 5, x + w, y + h - 5)
            g2d.drawLine(x, y, x + w - 5, y)
            g2d.drawLine(x, y + h - 1, x + w - 5, y + h - 1)
          } else {
            g2d.drawLine(x, y, x + w, y)
            g2d.drawLine(x, y + h - 1, x + w, y + h - 1)
          }

          if (index != tabbedPane.getSelectedIndex - 1 && index != getComponentCount - 1) {
            g2d.setColor(InterfaceColors.tabSeparator())
            g2d.drawLine(x + w - 1, y + 5, x + w - 1, y + h - 5)
          }
        }

        index += 1

      case _ =>
    }

    super.paintComponent(g)
  }
}

class CardPanel extends JPanel(null) with Transparent {
  private var shown: Option[Component] = None

  override def add(component: Component): Component = {
    super.add(component)

    if (shown.isEmpty)
      shown = Option(component)

    component
  }

  override def remove(component: Component): Unit = {
    val index = getComponents.indexOf(component)

    super.remove(component)

    if (getComponents.isEmpty) {
      shown = None
    } else if (shown.contains(component)) {
      shown = Option(getComponent((index - 1).max(0)))
    }
  }

  override def doLayout(): Unit = {
    getComponents.foreach{ component =>
      component.setBounds(0, 0, getWidth.max(component.getPreferredSize.width),
                          getHeight.max(component.getPreferredSize.height))
      component.setVisible(shown.contains(component))
    }
  }

  override def getPreferredSize: Dimension = {
    new Dimension(getComponents.maxBy(_.getPreferredSize.width)
                               .getPreferredSize.width.max(super.getPreferredSize.width),
                  getComponents.maxBy(_.getPreferredSize.height)
                               .getPreferredSize.height.max(super.getPreferredSize.height))
  }

  def show(component: Component): Unit = {
    shown = Option(component)

    revalidate()
    repaint()
  }

  def getShown: Component =
    shown.orNull
}

// allows automatic adding of close button to arbitrary tabs (Isaac B 5/21/25)
trait CloseableTab extends Component {
  def close(): Unit
}

class TabLabel(startPane: FloatingTabbedPane, text: String, tab: Component)
  extends JPanel(new GridBagLayout) with MouseUtils {

  private var tabbedPane: FloatingTabbedPane = startPane
  private val textLabel = new JLabel(text)
  private var rawText = text

  private val xInsets = 14
  private val yInsets = 3

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      tabbedPane.interceptTabClick(tab, e)
    }
  })

  def setTabbedPane(tabbedPane: FloatingTabbedPane): Unit = {
    this.tabbedPane = tabbedPane
  }

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
    c.insets = new Insets(yInsets, xInsets, yInsets, xInsets)

    add(textLabel, c)

    tab match {
      case closeable: CloseableTab =>
        val button = new CloseButton

        button.addMouseListener(new MouseAdapter {
          override def mouseClicked(e: MouseEvent): Unit = {
            if (e.getButton == MouseEvent.BUTTON1)
              closeable.close()
          }
        })

        c.fill = GridBagConstraints.NONE
        c.weightx = 0
        c.insets = new Insets(yInsets, 0, yInsets, xInsets)

        add(button, c)

        closeButton = Some(button)

      case _ =>
    }
  }

  override def getPreferredSize: Dimension =
    new Dimension(boldWidth + closeButton.map(_.getPreferredSize.width + xInsets).getOrElse(0) + xInsets * 2,
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
}
