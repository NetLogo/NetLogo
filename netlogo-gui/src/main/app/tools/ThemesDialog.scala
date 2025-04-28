// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ AbstractAction, ButtonGroup, JLabel, JPanel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, DialogButton, Positioning, RadioButton }
import org.nlogo.theme.{ ClassicTheme, ColorTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }

class ThemesDialog(frame: Frame with ThemeSync) extends ToolDialog(frame, "themes") with ThemeSync {
  private lazy val prefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val panel = new JPanel(new GridBagLayout)

  private lazy val label = new JLabel(s"<html>${I18N.gui("text")}</html>")

  private lazy val classicButton = new RadioButton(new AbstractAction(I18N.gui("classic")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(ClassicTheme)
    }
  })

  private lazy val lightButton = new RadioButton(new AbstractAction(I18N.gui("light")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(LightTheme)
    }
  })

  private lazy val darkButton = new RadioButton(new AbstractAction(I18N.gui("dark")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(DarkTheme)
    }
  })

  private lazy val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => {
    setVisible(false)
  })

  private lazy val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => {
    setTheme(startTheme)
    setSelected(startTheme)

    setVisible(false)
  })

  private var startTheme: ColorTheme = LightTheme

  override def initGUI(): Unit = {
    setResizable(false)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    panel.add(label, c)

    c.insets = new Insets(0, 6, 6, 6)

    panel.add(lightButton, c)
    panel.add(darkButton, c)
    panel.add(classicButton, c)

    val themeButtons = new ButtonGroup

    themeButtons.add(classicButton)
    themeButtons.add(lightButton)
    themeButtons.add(darkButton)

    val buttonPanel = new ButtonPanel(Seq(okButton, cancelButton))

    getRootPane.setDefaultButton(okButton)

    c.anchor = GridBagConstraints.CENTER

    panel.add(buttonPanel, c)

    add(panel)

    pack()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible) {
      startTheme = InterfaceColors.getTheme

      setSelected(startTheme)

      Positioning.center(this, frame)
    }

    super.setVisible(visible)
  }

  private def setTheme(theme: ColorTheme): Unit = {
    InterfaceColors.setTheme(theme)

    prefs.put("colorTheme", theme match {
      case ClassicTheme => "classic"
      case LightTheme => "light"
      case DarkTheme => "dark"
    })

    frame.syncTheme()
  }

  private def setSelected(theme: ColorTheme): Unit = {
    theme match {
      case ClassicTheme => classicButton.setSelected(true)
      case LightTheme => lightButton.setSelected(true)
      case DarkTheme => darkButton.setSelected(true)
    }
  }

  override def syncTheme(): Unit = {
    panel.setBackground(InterfaceColors.dialogBackground())

    label.setForeground(InterfaceColors.dialogText())

    lightButton.syncTheme()
    darkButton.syncTheme()
    classicButton.syncTheme()

    okButton.syncTheme()
    cancelButton.syncTheme()
  }
}
