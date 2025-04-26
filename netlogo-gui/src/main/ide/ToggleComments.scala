// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.text.{ Document, JTextComponent }

import org.nlogo.core.I18N
import org.nlogo.editor.{ DocumentAction, RichDocument }, RichDocument._
import org.nlogo.swing.UserAction,
  UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction },
    KeyBindings.keystroke

class ToggleComments
  extends DocumentAction(I18N.gui.get("menu.edit.comment") + " / " + I18N.gui.get("menu.edit.uncomment"))
  with FocusedOnlyAction
  with MenuAction {

  category    = EditCategory
  group       = EditFormatGroup
  accelerator = keystroke(KeyEvent.VK_SEMICOLON, withMenu = true)

  override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
    val (startLine, endLine) =
      document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)

    var currentLine = startLine
    var break = false

    while (currentLine <= endLine && !break) {
      val lineStart = document.lineToStartOffset(currentLine)
      val lineEnd = document.lineToEndOffset(currentLine)
      val text = document.getText(lineStart, lineEnd - lineStart)
      val semicolonPos = text.indexOf(';')
      val allSpaces = (0 until semicolonPos)
        .forall(i => Character.isWhitespace(text.charAt(i)))
        if (!allSpaces || semicolonPos == -1) {
          document.insertBeforeLinesInRange(startLine, endLine, ";")
          break = true
        }
      currentLine += 1
    }

    if (break)
      return

    // Logic to uncomment the selected section
    for (line <- startLine to endLine) {
      val lineStart = document.lineToStartOffset(line)
      val lineEnd   = document.lineToEndOffset(line)
      val text      = document.getText(lineStart, lineEnd - lineStart)
      val semicolonPos = text.indexOf(';')
      if (semicolonPos != -1) {
        val allSpaces = (0 until semicolonPos)
          .forall(i => Character.isWhitespace(text.charAt(i)))
          if (allSpaces)
            document.remove(lineStart + semicolonPos, 1)
      }
    }
  }
}
