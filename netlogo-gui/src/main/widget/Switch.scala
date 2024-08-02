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

  label.setForeground(InterfaceColors.WIDGET_TEXT)

  backgroundColor = InterfaceColors.SWITCH_BACKGROUND

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(Box.createHorizontalStrut(6))
  add(label)
  add(Box.createHorizontalStrut(6))
  add(toggle)

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

  override def getPreferredSize(font: Font): Dimension = {
    val height: Int = toggle.getHeight + 12
    val width: Int = label.getWidth + toggle.getWidth + 18
    new Dimension(StrictMath.max(MINWIDTH, width), StrictMath.max(MINHEIGHT, height))
  }

  override def getMinimumSize = new Dimension(MINWIDTH, MINHEIGHT)
  override def getMaximumSize = new Dimension(10000, MINHEIGHT)

  def mouseWheelMoved(e: MouseWheelEvent) { isOn = ! (e.getWheelRotation >= 1) }

  protected class Toggle extends JPanel {
    setPreferredSize(new Dimension(10, 20))
    setMaximumSize(new Dimension(10, 20))

    addMouseListener(new MouseAdapter {
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
      g2d.setColor(InterfaceColors.SWITCH_TOGGLE_BACKGROUND_ON)
      g2d.fillOval(0, y, getWidth, getWidth)
      g2d.setColor(InterfaceColors.SWITCH_TOGGLE)
      g2d.fillOval(1, y + 1, getWidth - 2, getWidth - 2)
    }
  }
}
