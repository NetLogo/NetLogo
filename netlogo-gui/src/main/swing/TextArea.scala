// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JTextArea

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextArea(text: String, rows: Int, columns: Int)
  extends JTextArea(text, rows, columns) with ThemeSync {

  def this(text: String) = this(text, 0, 0)
  def this(rows: Int, columns: Int) = this("", rows, columns)
  def this() = this("", 0, 0)

  syncTheme()

  def syncTheme() {
    setBackground(InterfaceColors.TEXT_AREA_BACKGROUND)
    setForeground(InterfaceColors.TEXT_AREA_TEXT)
    setCaretColor(InterfaceColors.TEXT_AREA_TEXT)
  }
}
