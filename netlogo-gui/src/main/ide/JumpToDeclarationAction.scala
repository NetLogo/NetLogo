// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.ActionEvent
import javax.swing.text.{ Document, JTextComponent }

import org.nlogo.core.I18N
import org.nlogo.editor.{ DocumentAction }
import org.nlogo.swing.UserAction,
  UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction },
    KeyBindings.keystroke

class JumpToDeclarationAction() extends DocumentAction(I18N.gui.get("tabs.code.rightclick.jumptodeclaration"))
  with FocusedOnlyAction with MenuAction {
    accelerator = keystroke('E', withMenu = true)
    group       = EditFormatGroup
    category    = EditCategory

    def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      JumpToDeclaration.jumpToDeclaration(component.getCaretPosition, component)
    }

}
