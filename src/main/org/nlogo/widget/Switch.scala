// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.agent.BooleanConstraint
import org.nlogo.window.{Events, InterfaceColors, MultiErrorWidget}
import java.awt._
import event.{MouseWheelEvent, MouseEvent, MouseAdapter, MouseWheelListener}

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
  protected val channel = new Channel
  protected val dragger = new Dragger
  protected var nameChanged = false
  protected var _name = ""

  setBackground(InterfaceColors.SWITCH_BACKGROUND)
  setBorder(widgetBorder)
  setOpaque(true)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)
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

  override def getPreferredSize(font: Font): Dimension = {
    val fontMetrics: FontMetrics = getFontMetrics(font)
    val height: Int = (fontMetrics.getMaxDescent + fontMetrics.getMaxAscent) + 2 * BORDERY
    val width: Int = 6 * BORDERX + channel.getWidth + fontMetrics.stringWidth(displayName) + fontMetrics.stringWidth("Off")
    new Dimension(StrictMath.max(MINWIDTH, width), StrictMath.max(MINHEIGHT, height))
  }

  override def getMinimumSize = new Dimension(MINWIDTH, MINHEIGHT)
  override def getMaximumSize = new Dimension(10000, MINHEIGHT)

  override def doLayout() {
    super.doLayout()
    val scaleFactor: Float = getHeight.asInstanceOf[Float] / MINHEIGHT.asInstanceOf[Float]
    channel.setSize((CHANNEL_WIDTH * scaleFactor).asInstanceOf[Int], (CHANNEL_HEIGHT * scaleFactor).asInstanceOf[Int])
    channel.setLocation(BORDERX, (getHeight - channel.getHeight) / 2)
    dragger.setSize((channel.getWidth * 0.9).asInstanceOf[Int], (channel.getHeight * 0.35).asInstanceOf[Int])
    dragger.setLocation(BORDERX + (channel.getWidth - dragger.getWidth) / 2, channel.getY + (if (isOn) (0.1 * channel.getHeight).asInstanceOf[Int] else (channel.getHeight - dragger.getHeight - (0.1 * channel.getHeight).asInstanceOf[Int])))
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    val fontMetrics: FontMetrics = g.getFontMetrics
    val stringAscent: Int = fontMetrics.getMaxAscent
    val controlRect: Rectangle = channel.getBounds
    g.setColor(getForeground)
    g.drawString("On", controlRect.width + BORDERX,
      (getHeight - (2 * stringAscent) - (2 * BORDERY)) / 2 + stringAscent + 1)
    g.drawString("Off", controlRect.width + BORDERX,
      (getHeight - (2 * stringAscent) - (2 * BORDERY)) / 2 + 2 * stringAscent + 1)
    val controlLabelWidth: Int =
      StrictMath.max(fontMetrics.stringWidth("On"), fontMetrics.stringWidth("Off")) + controlRect.width + 2 * BORDERX
    g.setColor(getForeground)
    val shortString = org.nlogo.awt.Fonts.shortenStringToFit(
      displayName, getWidth - 3 * BORDERX - controlLabelWidth, fontMetrics)
    g.drawString(
      shortString,
      controlLabelWidth + 2 * BORDERX,
      (getHeight - fontMetrics.getHeight - (2 * BORDERY)) / 2 + stringAscent + 1)

    setToolTipText(if (shortString != displayName) displayName else null)
  }

  def mouseWheelMoved(e: MouseWheelEvent) { isOn = ! (e.getWheelRotation >= 1) }

  protected class Dragger extends javax.swing.JPanel {
    setBackground(InterfaceColors.SWITCH_HANDLE)
    setBorder(org.nlogo.swing.Utils.createWidgetBorder)
    setOpaque(true)
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Switch.this)
        isOn = ! isOn
      }
    })
  }

  protected class Channel extends javax.swing.JComponent {
    setOpaque(false)
    setBackground(org.nlogo.awt.Colors.mixColors(InterfaceColors.SWITCH_BACKGROUND, java.awt.Color.BLACK, 0.5))
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Channel.this)
        if (org.nlogo.awt.Mouse.hasButton1(e)) {
          isOn = ! isOn
        }
      }
    })

    override def paintComponent(g: Graphics) {
      val x: Int = (getWidth * 0.2).toInt
      val y: Int = (getHeight * 0.1).toInt
      val width: Int = (getWidth * 0.6).toInt
      val height: Int = (getHeight * 0.8).toInt
      g.setColor(getBackground)
      g.fillRect(x, y, width, height)
      org.nlogo.swing.Utils.createWidgetBorder.paintBorder(this, g, x, y, width, height)
    }
  }
}
