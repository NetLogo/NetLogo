// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.swing.{ BoxRow, Transparent, VerticalStrut }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LabeledEditor(editor: PropertyEditor[?], text: String) extends JPanel with Transparent with ThemeSync {
  private val label = new JLabel(text) {
    setFont(getFont.deriveFont(9.0f))
  }

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  add(editor)
  add(new VerticalStrut(3))
  add(new BoxRow(Seq(label, Box.createHorizontalGlue)))

  override def syncTheme(): Unit = {
    editor.syncTheme()

    label.setForeground(InterfaceColors.dialogText())
  }
}
