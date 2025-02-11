// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JTextArea

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextArea(rows: Int, columns: Int, text: String = "")
  extends JTextArea(text, rows, columns) with ThemeSync {

  syncTheme()

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.textAreaBackground)
    setForeground(InterfaceColors.textAreaText)
    setCaretColor(InterfaceColors.textAreaText)
  }
}
