// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

import org.nlogo.agent.BooleanConstraint
import org.nlogo.window.Events
import org.nlogo.window.MultiErrorWidget

object Switch {
  val BORDERX: Int = 3
  val BORDERY: Int = 3
  val MINWIDTH: Int = 90
  val CHANNEL_WIDTH: Int = 15
  val CHANNEL_HEIGHT: Int = 28
  val MINHEIGHT: Int = CHANNEL_HEIGHT + 5
}

abstract class Switch extends MultiErrorWidget with MouseWheelListener
  with org.nlogo.window.Events.AfterLoadEventHandler
  with PaintableSwitch {

  protected var constraint = new BooleanConstraint
  protected val channel = new Channel
  protected val dragger = new Dragger
  protected var nameChanged = false
  protected var _name = ""

  add(dragger)
  add(channel)

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
      doLayout()
    }
  }

  def name = _name
  def name_=(name: String) {
    this._name = name
    displayName(name)
    repaint()
  }

  override def updateConstraints() {
    if (_name.length > 0) { new Events.AddBooleanConstraintEvent(_name, isOn).raise(this) }
  }

  def mouseWheelMoved(e: MouseWheelEvent) { isOn = !(e.getWheelRotation >= 1) }

  protected class Dragger extends PaintableSwitchDragger {
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Switch.this)
        isOn = !isOn
      }
    })
  }

  protected class Channel extends PaintableSwitchChannel {
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Channel.this)
        if (org.nlogo.awt.Mouse.hasButton1(e)) {
          isOn = !isOn
        }
      }
    })
  }
}
