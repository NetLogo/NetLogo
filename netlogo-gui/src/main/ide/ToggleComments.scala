// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.text.{ Document, JTextComponent }

import org.nlogo.core.I18N
import org.nlogo.editor.{ DocumentAction, RichDocument }, RichDocument._
import org.nlogo.swing.UserAction,
  UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction },
    KeyBindings.keystroke

object ToggleComments {
  def perform(component: JTextComponent, document: Document): Unit = {
    val (startLine, endLine) = document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)

    val nonEmptyLines: Seq[String] = (startLine to endLine).map { line =>
      val start = document.lineToStartOffset(line)

      document.getText(start, document.lineToEndOffset(line) - start)
    }.filter(_.trim.nonEmpty)

    if (nonEmptyLines.isEmpty) {
      (startLine to endLine).foreach { line =>
        document.insertString(document.lineToStartOffset(line), "; ", null)
      }
    } else if (nonEmptyLines.forall(_.trim.startsWith(";"))) {
      (startLine to endLine).foreach { line =>
        val start = document.lineToStartOffset(line)
        val lineText = document.getText(start, document.lineToEndOffset(line) - start)
        val index = lineText.indexOf(';')

        if (index != -1) {
          if (lineText.size > index + 1 && lineText(index + 1).isWhitespace) {
            document.remove(start + index, 2)
          } else {
            document.remove(start + index, 1)
          }
        }
      }
    } else {
      val offset = nonEmptyLines.map(_.indexWhere(!_.isWhitespace)).min

      (startLine to endLine).foreach { line =>
        val start = document.lineToStartOffset(line)

        if (document.getText(start, document.lineToEndOffset(line) - start).trim.nonEmpty)
          document.insertString(start + offset, "; ", null)
      }
    }
  }
}

class ToggleComments
  extends DocumentAction(I18N.gui.get("menu.edit.comment") + " / " + I18N.gui.get("menu.edit.uncomment"))
  with FocusedOnlyAction
  with MenuAction {

  category    = EditCategory
  group       = EditFormatGroup
  accelerator = keystroke(KeyEvent.VK_SEMICOLON, withMenu = true)

  override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
    ToggleComments.perform(component, document)
  }
}
