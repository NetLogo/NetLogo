// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, Insets, RadialGradientPaint }
import java.awt.event.{ MouseEvent, MouseAdapter }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.agent.BooleanConstraint
import org.nlogo.core.I18N
import org.nlogo.swing.{ Utils, Zoomable }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

abstract class Switch extends MultiErrorWidget with Events.AfterLoadEvent.Handler with ThemeSync {
  protected var constraint = new BooleanConstraint
  protected val label = new JLabel(I18N.gui.get("edit.switch.previewName")) with Zoomable
  protected val toggle = new Toggle
  protected var nameChanged = false
  protected var _name = ""

  locally {
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
  }

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  setBorder(new AdaptableBorder(new Insets(6, 6, 6, 6), new Insets(8, 8, 8, 8)))

  add(label)
  add(Box.createHorizontalGlue)
  add(new AdaptableHorizontalStrut(6, 8))
  add(toggle)

  def isOn: Boolean = constraint.defaultValue.booleanValue

  def isOn_=(on: Boolean): Unit = {
    if (isOn != on) {
      constraint.defaultValue = on
      updateConstraints()
      repaint()
      new Events.DirtyEvent(None).raise(this)
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
      new Dimension(super.getPreferredSize.width, Utils.zoom(33))
    } else {
      new Dimension(super.getPreferredSize.width, Utils.zoom(40))
    }
  }

  override def getMinimumSize: Dimension = {
    Utils.zoomSize {
      if (_oldSize) {
        new Dimension(90, 33)
      } else {
        new Dimension(50, 40)
      }
    }
  }

  override def doLayout(): Unit = {
    super.doLayout()

    if (label.getPreferredSize.width > label.getWidth) {
      label.setToolTipText(label.getText)
    } else {
      label.setToolTipText(null)
    }
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.switchBackground())

    label.setForeground(InterfaceColors.widgetText())
  }

  protected class Toggle extends JPanel {
    private var hover = false

    override def getPreferredSize: Dimension =
      new Dimension(Utils.zoom(10), super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      new Dimension(getPreferredSize.width, Int.MaxValue)

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
