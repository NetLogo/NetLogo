// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.AbstractAction

import org.nlogo.core.LocaleContext
import org.nlogo.swing.UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction }

class JumpToDeclarationAction(editor: AbstractEditorArea)
  extends AbstractAction(loc"Jump To Declaration") with FocusedOnlyAction with MenuAction {

  accelerator = KeyBindings.keystroke('E', withMenu = true)
  group       = EditFormatGroup
  category    = EditCategory
  mnemonic    = KeyEvent.VK_J

  override def actionPerformed(e: ActionEvent): Unit = {
    JumpToDeclaration.jumpToDeclaration(editor.getCaretPosition, editor)
  }
}
