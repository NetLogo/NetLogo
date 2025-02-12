// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ AbstractAction, ButtonGroup, JPanel }

import org.nlogo.app.App
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, ButtonPanel, Positioning, RadioButton }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class ThemesDialog(frame: Frame) extends ToolDialog(frame, "themes") with ThemeSync {
  private lazy val prefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val panel = new JPanel(new GridBagLayout)

  private lazy val classicButton = new RadioButton(new AbstractAction(I18N.gui("classic")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme("classic")
    }
  })

  private lazy val lightButton = new RadioButton(new AbstractAction(I18N.gui("light")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme("light")
    }
  })

  private lazy val darkButton = new RadioButton(new AbstractAction(I18N.gui("dark")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme("dark")
    }
  })

  private lazy val okButton = new Button(I18N.gui.get("common.buttons.ok"), () => {
    setVisible(false)
  })

  private lazy val cancelButton = new Button(I18N.gui.get("common.buttons.cancel"), () => {
    setTheme(startTheme)
    setSelected(startTheme)

    setVisible(false)
  })

  private var startTheme = "light"

  override def initGUI(): Unit = {
    setResizable(false)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    panel.add(lightButton, c)

    c.insets = new Insets(0, 6, 6, 6)

    panel.add(darkButton, c)
    panel.add(classicButton, c)

    val themeButtons = new ButtonGroup

    themeButtons.add(classicButton)
    themeButtons.add(lightButton)
    themeButtons.add(darkButton)

    val buttonPanel = new ButtonPanel(Array(okButton, cancelButton))

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

  private def setTheme(theme: String): Unit = {
    InterfaceColors.setTheme(theme)

    prefs.put("colorTheme", theme)

    App.app.syncWindowThemes()
  }

  private def setSelected(theme: String): Unit = {
    theme match {
      case "classic" => classicButton.setSelected(true)
      case "light" => lightButton.setSelected(true)
      case "dark" => darkButton.setSelected(true)
    }
  }

  override def syncTheme(): Unit = {
    panel.setBackground(InterfaceColors.dialogBackground)

    lightButton.syncTheme()
    darkButton.syncTheme()
    classicButton.syncTheme()

    okButton.syncTheme()
    cancelButton.syncTheme()
  }
}
