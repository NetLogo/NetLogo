// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.text.{ DefaultEditorKit, JTextComponent, TextAction }

// helpers for applying correct shortcuts to text fields and text areas (Isaac B 6/19/25)
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

  class CorrectPreviousWordAction(comp: JTextComponent, select: Boolean)
    extends TextAction("previous word") {

    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getCaretPosition > 0) {
        if (comp.getText()(comp.getCaretPosition - 1).isLetterOrDigit) {
          while (comp.getCaretPosition > 0 && comp.getText()(comp.getCaretPosition - 1).isLetterOrDigit) {
            if (select) {
              comp.moveCaretPosition(comp.getCaretPosition - 1)
            } else {
              comp.setCaretPosition(comp.getCaretPosition - 1)
            }
          }
        } else if (select) {
          comp.moveCaretPosition(comp.getCaretPosition - 1)
        } else {
          comp.setCaretPosition(comp.getCaretPosition - 1)
        }
      }
    }
  }

  class CorrectNextWordAction(comp: JTextComponent, select: Boolean) extends TextAction("next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getCaretPosition < comp.getText().size) {
        if (comp.getText()(comp.getCaretPosition).isLetterOrDigit) {
          while (comp.getCaretPosition < comp.getText().size &&
                 comp.getText()(comp.getCaretPosition).isLetterOrDigit) {
            if (select) {
              comp.moveCaretPosition(comp.getCaretPosition + 1)
            } else {
              comp.setCaretPosition(comp.getCaretPosition + 1)
            }
          }
        } else if (select) {
          comp.moveCaretPosition(comp.getCaretPosition + 1)
        } else {
          comp.setCaretPosition(comp.getCaretPosition + 1)
        }
      }
    }
  }

  class CorrectSelectWordAction(comp: JTextComponent) extends TextAction("select word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectPreviousWordAction(comp, false).actionPerformed(e)
      new CorrectNextWordAction(comp, true).actionPerformed(e)
    }
  }

  class CorrectDeletePrevWordAction(comp: JTextComponent) extends TextAction("delete previous word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectPreviousWordAction(comp, true).actionPerformed(e)

      comp.replaceSelection(null)
    }
  }

  class CorrectDeleteNextWordAction(comp: JTextComponent) extends TextAction("delete next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectNextWordAction(comp, true).actionPerformed(e)

      comp.replaceSelection(null)
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

  class CorrectForwardAction(comp: JTextComponent) extends TextAction("caret backward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getSelectionStart == comp.getSelectionEnd) {
        comp.setCaretPosition((comp.getCaretPosition + 1).min(comp.getText.size))
      } else {
        comp.setCaretPosition(comp.getSelectionEnd)
      }
    }
  }
}
