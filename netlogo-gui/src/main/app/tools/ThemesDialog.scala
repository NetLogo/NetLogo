// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ AbstractAction, ButtonGroup, JButton, JPanel, JRadioButton }

import org.nlogo.core.I18N
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class ThemesDialog(frame: Frame with ThemeSync) extends ToolDialog(frame, "themes") with ThemeSync {
  private lazy val prefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val classicButton = new JRadioButton(new AbstractAction(I18N.gui("classic")) {
    def actionPerformed(e: ActionEvent) {
      setTheme("classic")
    }
  })

  private lazy val lightButton = new JRadioButton(new AbstractAction(I18N.gui("light")) {
    def actionPerformed(e: ActionEvent) {
      setTheme("light")
    }
  })

  private lazy val darkButton = new JRadioButton(new AbstractAction(I18N.gui("dark")) {
    def actionPerformed(e: ActionEvent) {
      setTheme("dark")
    }
  })

  private var startTheme = ""

  override def initGUI() {
    setResizable(false)

    val panel = new JPanel

    panel.setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    panel.add(classicButton, c)

    c.insets = new Insets(0, 6, 6, 6)

    panel.add(lightButton, c)
    panel.add(darkButton, c)

    val themeButtons = new ButtonGroup

    themeButtons.add(classicButton)
    themeButtons.add(lightButton)
    themeButtons.add(darkButton)

    val buttonPanel = new JPanel

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.ok")) {
      def actionPerformed(e: ActionEvent) {
        setVisible(false)
      }
    }))

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent) {
        setTheme(startTheme)
        setSelected(startTheme)

        setVisible(false)
      }
    }))

    panel.add(buttonPanel, c)

    add(panel)

    pack()
  }

  override def setVisible(visible: Boolean) {
    if (visible) {
      startTheme = InterfaceColors.getTheme

      setSelected(startTheme)
    }

    super.setVisible(visible)
  }

  private def setTheme(theme: String) {
    InterfaceColors.setTheme(theme)

    prefs.put("colorTheme", theme)

    frame.syncTheme()
  }

  private def setSelected(theme: String) {
    theme match {
      case "classic" => classicButton.setSelected(true)
      case "light" => lightButton.setSelected(true)
      case "dark" => darkButton.setSelected(true)
    }
  }

  def syncTheme() {
    
  }
}
