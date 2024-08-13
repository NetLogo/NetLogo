// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, ButtonGroup, JButton, JPanel, JRadioButton }

import org.nlogo.core.I18N
import org.nlogo.window.InterfaceColors

class ThemesDialog(frame: Frame) extends ToolDialog(frame, "themes") {
  override def initGUI() {
    setResizable(false)

    val panel = new JPanel

    panel.setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    val classicButton = new JRadioButton(new AbstractAction(I18N.gui("classic")) {
      def actionPerformed(e: ActionEvent) {
        InterfaceColors.setTheme(InterfaceColors.ClassicTheme)
      }
    })

    panel.add(classicButton, c)

    c.insets = new Insets(0, 6, 6, 6)

    val lightButton = new JRadioButton(new AbstractAction(I18N.gui("light")) {
      def actionPerformed(e: ActionEvent) {
        InterfaceColors.setTheme(InterfaceColors.LightTheme)
      }
    })

    panel.add(lightButton, c)

    val startTheme = InterfaceColors.getTheme

    startTheme match {
      case InterfaceColors.ClassicTheme => classicButton.setSelected(true)
      case InterfaceColors.LightTheme => lightButton.setSelected(true)
    }

    val themeButtons = new ButtonGroup

    themeButtons.add(classicButton)
    themeButtons.add(lightButton)

    val buttonPanel = new JPanel

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.ok")) {
      def actionPerformed(e: ActionEvent) {
        setVisible(false)
      }
    }))

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent) {
        InterfaceColors.setTheme(startTheme)

        setVisible(false)
      }
    }))

    panel.add(buttonPanel, c)

    add(panel)

    pack()
  }
}
