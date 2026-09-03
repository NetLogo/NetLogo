// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JButton, JToggleButton }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class Button(action: Action) extends JButton(action) with RoundedBorderPanel with Zoomable with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  enableHover()
  enablePressed()
  setBorder(new ZoomableBorder(3, 12, 3, 12))
  setFocusable(false)
  setContentAreaFilled(false)
  setDiameter(Utils.zoom(6))

  syncTheme()

  override def zoomComponent(): Unit = {
    setDiameter(Utils.zoom(6))
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground())
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
    setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
    setBorderColor(InterfaceColors.toolbarControlBorder())
    setForeground(InterfaceColors.toolbarText())
  }
}

class ToggleButton(action: Action) extends JToggleButton(action) with RoundedBorderPanel with Zoomable with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  enableHover()
  enablePressed()
  setBorder(new ZoomableBorder(3, 12, 3, 12))
  setFocusable(false)
  setContentAreaFilled(false)
  setDiameter(Utils.zoom(6))

  syncTheme()

  override def isHover: Boolean =
    super.isHover || isSelected

  override def zoomComponent(): Unit = {
    setDiameter(Utils.zoom(6))
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground())
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
    setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
    setBorderColor(InterfaceColors.toolbarControlBorder())
    setForeground(InterfaceColors.toolbarText())
  }
}

class DialogButton(primary: Boolean, action: Action) extends Button(action) {
  def this(primary: Boolean, text: String, function: () => Unit) = this(primary, new AbstractAction(text) {
    override def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  def this(primary: Boolean, text: String, function: (String) => Unit) = this(primary, text, () => function(text))

  setFocusable(true)

  syncTheme()

  override def syncTheme(): Unit = {
    if (primary) {
      setBackgroundColor(InterfaceColors.primaryButtonBackground())
      setBackgroundHoverColor(InterfaceColors.primaryButtonBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.primaryButtonBackgroundPressed())
      setBorderColor(InterfaceColors.primaryButtonBorder())
      setForeground(InterfaceColors.primaryButtonText())
    } else {
      setBackgroundColor(InterfaceColors.secondaryButtonBackground())
      setBackgroundHoverColor(InterfaceColors.secondaryButtonBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.secondaryButtonBackgroundPressed())
      setBorderColor(InterfaceColors.secondaryButtonBorder())
      setForeground(InterfaceColors.secondaryButtonText())
    }
  }
}
