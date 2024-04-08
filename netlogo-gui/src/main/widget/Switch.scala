// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.agent.BooleanConstraint
import org.nlogo.window.{ Events, InterfaceColors, MultiErrorWidget }
import java.awt._
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
  protected val checkBox = new CheckBox
  protected var nameChanged = false
  protected var _name = ""

  setBackground(InterfaceColors.SWITCH_BACKGROUND)
  setBorder(widgetBorder)
  setOpaque(true)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)
  setLayout(new FlowLayout(FlowLayout.LEFT))
  add(checkBox)
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
    checkBox.setText(displayName)
    repaint()
  }

  override def updateConstraints() {
    if (_name.length > 0) { new Events.AddBooleanConstraintEvent(_name, isOn).raise(this) }
  }

  override def getPreferredSize(font: Font): Dimension = {
    val fontMetrics: FontMetrics = getFontMetrics(font)
    val height: Int = (fontMetrics.getMaxDescent + fontMetrics.getMaxAscent) + 2 * BORDERY
    val width: Int = 6 * BORDERX + checkBox.getWidth
    new Dimension(StrictMath.max(MINWIDTH, width), StrictMath.max(MINHEIGHT, height))
  }

  override def getMinimumSize = new Dimension(MINWIDTH, MINHEIGHT)
  override def getMaximumSize = new Dimension(10000, MINHEIGHT)

  def mouseWheelMoved(e: MouseWheelEvent) { isOn = ! (e.getWheelRotation >= 1) }

  protected class CheckBox extends javax.swing.JCheckBox {
    setFont(getFont.deriveFont(12f))
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(CheckBox.this)
        isOn = !isOn
      }
    })
  }
}
