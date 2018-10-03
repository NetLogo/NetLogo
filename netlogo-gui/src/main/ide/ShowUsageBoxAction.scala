// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.ActionEvent
import javax.swing.text.{ Document, JTextComponent }

import org.nlogo.editor.{ AbstractEditorArea, DocumentAction }
import org.nlogo.swing.UserAction,
  UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction },
    KeyBindings.keystroke

class ShowUsageBoxAction(showUsageBox: ShowUsageBox) extends DocumentAction("Show Usage")
  with FocusedOnlyAction with MenuAction {
    accelerator = keystroke('U', withMenu = true)
    group       = EditFormatGroup
    category    = EditCategory

    def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      if (component.isInstanceOf[AbstractEditorArea]) {
        val editor = component.asInstanceOf[AbstractEditorArea]
        showUsageBox.init(editor)
      }

      val caretScreenPosition = component.getCaret.getMagicCaretPosition
      val screenPosition = component.getLocationOnScreen
      screenPosition.x += caretScreenPosition.x;
      screenPosition.y += caretScreenPosition.y;
      showUsageBox.showBox(screenPosition, component.getCaretPosition)
    }

}
