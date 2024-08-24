// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.agent.BooleanConstraint
import org.nlogo.swing.Utils
import org.nlogo.window.{ Events, InterfaceColors, MultiErrorWidget }

import java.awt._
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }
import event.{ MouseWheelEvent, MouseEvent, MouseAdapter, MouseWheelListener }

object Switch {
  val BORDERX: Int = 3
  val BORDERY: Int = 3
  val MINWIDTH: Int = 90
  val CHANNEL_WIDTH: Int = 15
  val CHANNEL_HEIGHT: Int = 28
  val MINHEIGHT: Int = CHANNEL_HEIGHT + 5
}

abstract class Switch extends MultiErrorWidget with MouseWheelListener
  with org.nlogo.window.Events.AfterLoadEvent.Handler {

  import Switch._

  protected var constraint = new BooleanConstraint
  protected val label = new JLabel
  protected val toggle = new Toggle
  protected var nameChanged = false
  protected var _name = ""

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  if (preserveWidgetSizes) {
    add(Box.createHorizontalStrut(6))
    add(label)
    add(Box.createHorizontalStrut(6))
    add(toggle)
  }

  else {
    add(Box.createHorizontalStrut(12))
    add(label)
    add(Box.createHorizontalStrut(12))
    add(toggle)
    add(Box.createHorizontalStrut(12))
  }

  addMouseWheelListener(this)
  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      new Events.InputBoxLoseFocusEvent().raise(Switch.this)
    }
  })

  def isOn = constraint.defaultValue.booleanValue

  def isOn_=(on: Boolean) {
    if (isOn != on) {
      constraint.defaultValue = on
      updateConstraints()
      repaint()
      new Events.WidgetEditedEvent(this).raise(this)
    }
  }

  def name = _name
  def name_=(name: String) {
    this._name = name
    displayName(name)
    label.setText(displayName)
    repaint()
  }

  override def updateConstraints() {
    if (_name.length > 0) { new Events.AddBooleanConstraintEvent(_name, isOn).raise(this) }
  }

  override def getPreferredSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(MINWIDTH.max(label.getWidth + toggle.getWidth + 18), MINHEIGHT.max(37))
    else
      super.getPreferredSize

  override def getMinimumSize =
    if (preserveWidgetSizes)
      new Dimension(MINWIDTH, MINHEIGHT)
    else
      new Dimension(50, 37)

  override def getMaximumSize =
    if (preserveWidgetSizes)
      new Dimension(10000, MINHEIGHT)
    else
      new Dimension(10000, 37)

  def mouseWheelMoved(e: MouseWheelEvent) { isOn = ! (e.getWheelRotation >= 1) }

  override def paintComponent(g: Graphics) {
    backgroundColor = InterfaceColors.SWITCH_BACKGROUND

    label.setForeground(InterfaceColors.WIDGET_TEXT)

    super.paintComponent(g)
  }

  protected class Toggle extends JPanel {
    private var hover = false

    locally {
      val size =
        if (preserveWidgetSizes)
          new Dimension(10, 20)
        else
          new Dimension(10, 25)

      setPreferredSize(size)
      setMinimumSize(size)
      setMaximumSize(size)
    }

    setOpaque(false)

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent) {
        hover = true

        repaint()
      }

      override def mouseExited(e: MouseEvent) {
        hover = false

        repaint()
      }

      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Toggle.this)
        isOn = !isOn
      }
    })

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      if (isOn)
        g2d.setColor(InterfaceColors.SWITCH_TOGGLE_BACKGROUND_ON)
      else
        g2d.setColor(InterfaceColors.SWITCH_TOGGLE_BACKGROUND_OFF)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, getWidth, getWidth)
      val y = if (isOn) 0 else getHeight - getWidth
      val d = if (isOn) 3 else -3
      if (hover) {
        g2d.setPaint(new RadialGradientPaint(getWidth / 2f, y + getWidth / 2f + d, getWidth / 2f, Array(0f, 1f),
                                             Array(InterfaceColors.WIDGET_HOVER_SHADOW,
                                                   InterfaceColors.TRANSPARENT)))
        g2d.fillOval(0, y + d, getWidth, getWidth)
      }
      g2d.setColor(InterfaceColors.SWITCH_TOGGLE_BACKGROUND_ON)
      g2d.fillOval(0, y, getWidth, getWidth)
      g2d.setColor(InterfaceColors.SWITCH_TOGGLE)
      g2d.fillOval(1, y + 1, getWidth - 2, getWidth - 2)
    }
  }
}
