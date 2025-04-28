// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.JTextArea
import javax.swing.text.{ DefaultEditorKit, TextAction }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextArea(rows: Int, columns: Int, text: String = "")
  extends JTextArea(text, rows, columns) with ThemeSync {

  getActionMap.put(DefaultEditorKit.backwardAction, new CorrectBackwardAction)
  getActionMap.put(DefaultEditorKit.forwardAction, new CorrectForwardAction)

  syncTheme()

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.textAreaBackground())
    setForeground(InterfaceColors.textAreaText())
    setCaretColor(InterfaceColors.textAreaText())
  }

  private class CorrectBackwardAction extends TextAction("caret backward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (getSelectionStart == getSelectionEnd) {
        setCaretPosition((getCaretPosition - 1).max(0))
      } else {
        setCaretPosition(getSelectionStart)
      }
    }
  }

  private class CorrectForwardAction extends TextAction("caret backward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (getSelectionStart == getSelectionEnd) {
        setCaretPosition((getCaretPosition + 1).min(getText.size))
      } else {
        setCaretPosition(getSelectionEnd)
      }
    }
  }
}
