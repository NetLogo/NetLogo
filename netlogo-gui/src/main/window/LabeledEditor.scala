// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout
import javax.swing.{ JLabel, JPanel }

import org.nlogo.swing.Transparent
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LabeledEditor(editor: PropertyEditor[_], text: String)
  extends JPanel(new BorderLayout(0, 3)) with Transparent with ThemeSync {

  private val label = new JLabel(text) {
    setFont(getFont.deriveFont(9.0f))
  }

  add(editor, BorderLayout.CENTER)
  add(label, BorderLayout.SOUTH)

  override def syncTheme(): Unit = {
    editor.syncTheme()

    label.setForeground(InterfaceColors.dialogText())
  }
}
