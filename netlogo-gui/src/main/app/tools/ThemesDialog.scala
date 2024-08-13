// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, ButtonGroup, JButton, JPanel, JRadioButton }

import org.nlogo.core.I18N

class ThemesDialog(frame: Frame) extends ToolDialog(frame, "themes") {
  lazy val themeButtons = new ButtonGroup

  override def initGUI() {
    setResizable(false)

    val panel = new JPanel

    panel.setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    val classicButton = new JRadioButton(I18N.gui("classic"))

    themeButtons.add(classicButton)
    panel.add(classicButton, c)

    c.insets = new Insets(0, 6, 6, 6)

    val lightButton = new JRadioButton(I18N.gui("light"))

    themeButtons.add(lightButton)
    panel.add(lightButton, c)

    lightButton.setSelected(true)

    val buttonPanel = new JPanel

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.ok")) {
      def actionPerformed(e: ActionEvent) {
        // confirm changes

        setVisible(false)
      }
    }))

    buttonPanel.add(new JButton(new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent) {
        // revert changes

        setVisible(false)
      }
    }))

    panel.add(buttonPanel, c)

    add(panel)

    pack()
  }
}
