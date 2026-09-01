// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LabeledEditor(editor: PropertyEditor[?], text: String) extends BoxColumn(3) with ThemeSync {
  private val label = new JLabel(text) {
    setFont(getFont.deriveFont(9.0f))
  }

  add(new BoxRow(editor, BoxAlign.Start))
  add(new BoxRow(label, BoxAlign.Start))

  override def syncTheme(): Unit = {
    editor.syncTheme()

    label.setForeground(InterfaceColors.dialogText())
  }
}
