// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.core.I18N
import org.nlogo.swing.UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction }

class JumpToDeclarationAction(editor: AbstractEditorArea)
  extends AbstractAction(I18N.gui.get("tabs.code.rightclick.jumptodeclaration")) with FocusedOnlyAction
  with MenuAction {

  accelerator = KeyBindings.keystroke('E', withMenu = true)
  group       = EditFormatGroup
  category    = EditCategory

  override def actionPerformed(e: ActionEvent): Unit = {
    JumpToDeclaration.jumpToDeclaration(editor.getCaretPosition, editor)
  }
}
