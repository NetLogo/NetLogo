// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.BooleanConstraint
import org.nlogo.core.I18N
import org.nlogo.swing.Utils
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import java.awt._
import javax.swing.{ JLabel, JPanel }
import event.{ MouseWheelEvent, MouseEvent, MouseAdapter, MouseWheelListener }

object Switch {
  val MINWIDTH: Int = 90
  val CHANNEL_HEIGHT: Int = 28
  val MINHEIGHT: Int = CHANNEL_HEIGHT + 5
}

abstract class Switch extends MultiErrorWidget with MouseWheelListener
  with Events.AfterLoadEvent.Handler with ThemeSync {

  import Switch._

  protected var constraint = new BooleanConstraint
  protected val label = new JLabel(I18N.gui.get("edit.switch.previewName"))
  protected val toggle = new Toggle
  protected var nameChanged = false
  protected var _name = ""

  locally {
    setLayout(new GridBagLayout)

    initGUI()

    addMouseWheelListener(this)

    val mouseListener = new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        new Events.InputBoxLoseFocusEvent().raise(Switch.this)

        if (e.getButton == MouseEvent.BUTTON1)
          isOn = !isOn
      }
    }

    addMouseListener(mouseListener)
    label.addMouseListener(mouseListener)
    toggle.addMouseListener(mouseListener)

    if (_boldName)
      label.setFont(label.getFont.deriveFont(Font.BOLD))
  }

  override def initGUI(): Unit = {
    removeAll()

    if (_oldSize) {
      val c = new GridBagConstraints

      c.insets = new Insets(0, zoom(6), 0, zoom(6))
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1

      add(label, c)

      c.insets = new Insets(zoom(6), 0, zoom(6), zoom(6))
      c.fill = GridBagConstraints.VERTICAL
      c.weightx = 0
      c.weighty = 1
      c.anchor = GridBagConstraints.EAST

      add(toggle, c)
    } else {
      val c = new GridBagConstraints

      c.insets = new Insets(0, zoom(8), 0, zoom(8))
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1

      add(label, c)

      c.insets = new Insets(zoom(8), 0, zoom(8), zoom(8))
      c.fill = GridBagConstraints.VERTICAL
      c.weightx = 0
      c.weighty = 1
      c.anchor = GridBagConstraints.EAST

      add(toggle, c)
    }
  }

  def isOn: Boolean = constraint.defaultValue.booleanValue

  def isOn_=(on: Boolean): Unit = {
    if (isOn != on) {
      constraint.defaultValue = on
      updateConstraints()
      repaint()
      new Events.WidgetEditedEvent(this).raise(this)
    }
  }

  def name: String = _name
  def setVarName(name: String): Unit = {
    this._name = name
    if (name == "") {
      displayName(I18N.gui.get("edit.switch.previewName"))
    } else {
      displayName(name)
    }
    label.setText(displayName)
    repaint()
  }

  override def updateConstraints(): Unit = {
    if (_name.length > 0) { new Events.AddBooleanConstraintEvent(_name, isOn).raise(this) }
  }

  override def getPreferredSize: Dimension = {
    if (_oldSize) {
      new Dimension(super.getPreferredSize.width, MINHEIGHT)
    } else {
      new Dimension(super.getPreferredSize.width, 37)
    }
  }

  override def getMinimumSize: Dimension = {
    if (_oldSize) {
      new Dimension(MINWIDTH, MINHEIGHT)
    } else {
      new Dimension(50, 40)
    }
  }

  def mouseWheelMoved(e: MouseWheelEvent): Unit = { isOn = ! (e.getWheelRotation >= 1) }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.switchBackground())

    label.setForeground(InterfaceColors.widgetText())
  }

  protected class Toggle extends JPanel {
    private var hover = false

    override def getMinimumSize: Dimension =
      new Dimension(zoom(10), super.getPreferredSize.height)

    override def getPreferredSize: Dimension =
      getMinimumSize

    override def getMaximumSize: Dimension =
      getMinimumSize

    setOpaque(false)

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent): Unit = {
        hover = true

        repaint()
      }

      override def mouseExited(e: MouseEvent): Unit = {
        hover = false

        repaint()
      }
    })

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)
      if (isOn) {
        g2d.setColor(InterfaceColors.switchToggleBackgroundOn())
      } else {
        g2d.setColor(InterfaceColors.switchToggleBackgroundOff())
      }
      g2d.fillRoundRect(0, 0, getWidth, getHeight, getWidth, getWidth)
      val y = if (isOn) 0 else getHeight - getWidth
      val d = if (isOn) 3 else -3
      if (hover) {
        g2d.setPaint(new RadialGradientPaint(getWidth / 2f, y + getWidth / 2f + d, getWidth / 2f, Array(0f, 1f),
                                             Array(InterfaceColors.widgetHoverShadow(),
                                                   InterfaceColors.Transparent)))
        g2d.fillOval(0, y + d, getWidth, getWidth)
      }
      g2d.setColor(InterfaceColors.switchToggleBackgroundOn())
      g2d.fillOval(0, y, getWidth, getWidth)
      g2d.setColor(InterfaceColors.switchToggle())
      g2d.fillOval(1, y + 1, getWidth - 2, getWidth - 2)
    }
  }
}
