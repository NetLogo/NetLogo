// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.text.{ DefaultEditorKit, JTextComponent, TextAction }

// helpers for applying correct shortcuts to text components (Isaac B 6/19/25)
object TextActions {
  def applyToComponent(comp: JTextComponent): Unit = {
    comp.getActionMap.put(DefaultEditorKit.previousWordAction, new CorrectPreviousWordAction(comp, false))
    comp.getActionMap.put(DefaultEditorKit.selectionPreviousWordAction, new CorrectPreviousWordAction(comp, true))
    comp.getActionMap.put(DefaultEditorKit.nextWordAction, new CorrectNextWordAction(comp, false))
    comp.getActionMap.put(DefaultEditorKit.selectionNextWordAction, new CorrectNextWordAction(comp, true))
    comp.getActionMap.put(DefaultEditorKit.selectWordAction, new CorrectSelectWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.deletePrevWordAction, new CorrectDeletePrevWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.deleteNextWordAction, new CorrectDeleteNextWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.backwardAction, new CorrectBackwardAction(comp))
    comp.getActionMap.put(DefaultEditorKit.forwardAction, new CorrectForwardAction(comp))
  }

  // copied from org.nlogo.lex.Charset, which is used during lexing
  // to determine valid NetLogo identifiers (Isaac B 12/29/25)
  private def isIdentChar(c: Char): Boolean =
    c.isLetterOrDigit || "_.?=*!<>:#+/%$^'&-".contains(c)

  class CorrectPreviousWordAction(comp: JTextComponent, select: Boolean)
    extends TextAction("previous word") {

    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getCaretPosition > 0) {
        val text = comp.getText

        while (comp.getCaretPosition > 0 && !isIdentChar(text(comp.getCaretPosition - 1))) {
          if (select) {
            comp.moveCaretPosition(comp.getCaretPosition - 1)
          } else {
            comp.setCaretPosition(comp.getCaretPosition - 1)
          }
        }

        while (comp.getCaretPosition > 0 && isIdentChar(text(comp.getCaretPosition - 1))) {
          if (select) {
            comp.moveCaretPosition(comp.getCaretPosition - 1)
          } else {
            comp.setCaretPosition(comp.getCaretPosition - 1)
          }
        }
      }
    }
  }

  class CorrectNextWordAction(comp: JTextComponent, select: Boolean) extends TextAction("next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      val text = comp.getText

      if (comp.getCaretPosition < text.size) {
        while (comp.getCaretPosition < text.size && !isIdentChar(text(comp.getCaretPosition))) {
          if (select) {
            comp.moveCaretPosition(comp.getCaretPosition + 1)
          } else {
            comp.setCaretPosition(comp.getCaretPosition + 1)
          }
        }

        while (comp.getCaretPosition < text.size && isIdentChar(text(comp.getCaretPosition))) {
          if (select) {
            comp.moveCaretPosition(comp.getCaretPosition + 1)
          } else {
            comp.setCaretPosition(comp.getCaretPosition + 1)
          }
        }
      }
    }
  }

  class CorrectSelectWordAction(comp: JTextComponent) extends TextAction("select word") {
    def actionPerformed(e: ActionEvent): Unit = {
      val text = comp.getText

      if (text.nonEmpty) {
        val start = isIdentChar(text((comp.getCaretPosition).min(text.size - 1))) ||
                    isIdentChar(text((comp.getCaretPosition - 1).max(0)))

        while (comp.getCaretPosition > 0 && isIdentChar(text(comp.getCaretPosition - 1)) == start)
          comp.setCaretPosition(comp.getCaretPosition - 1)

        while (comp.getCaretPosition < text.size && isIdentChar(text(comp.getCaretPosition)) == start)
          comp.moveCaretPosition(comp.getCaretPosition + 1)
      }
    }
  }

  class CorrectDeletePrevWordAction(comp: JTextComponent) extends TextAction("delete previous word") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getCaretPosition > 0) {
        val text = comp.getText
        val start = isIdentChar(text(comp.getCaretPosition - 1))

        while (comp.getCaretPosition > 0 && isIdentChar(text(comp.getCaretPosition - 1)) == start)
          comp.moveCaretPosition(comp.getCaretPosition - 1)

        comp.replaceSelection(null)
        comp.setCaretPosition(comp.getCaretPosition)
      }
    }
  }

  class CorrectDeleteNextWordAction(comp: JTextComponent) extends TextAction("delete next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      val text = comp.getText

      if (comp.getCaretPosition < text.size) {
        val startPos = comp.getCaretPosition
        val start = isIdentChar(text(startPos))

        while (comp.getCaretPosition < text.size && isIdentChar(text(comp.getCaretPosition)) == start)
          comp.moveCaretPosition(comp.getCaretPosition + 1)

        comp.replaceSelection(null)
        comp.setCaretPosition(startPos)
      }
    }
  }

  class CorrectBackwardAction(comp: JTextComponent) extends TextAction("caret backward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getSelectionStart == comp.getSelectionEnd) {
        comp.setCaretPosition((comp.getCaretPosition - 1).max(0))
      } else {
        comp.setCaretPosition(comp.getSelectionStart)
      }
    }
  }

  class CorrectForwardAction(comp: JTextComponent) extends TextAction("caret forward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getSelectionStart == comp.getSelectionEnd) {
        comp.setCaretPosition((comp.getCaretPosition + 1).min(comp.getText.size))
      } else {
        comp.setCaretPosition(comp.getSelectionEnd)
      }
    }
  }
}
