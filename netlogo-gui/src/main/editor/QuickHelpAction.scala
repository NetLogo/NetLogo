// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Component, KeyboardFocusManager }
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, SwingUtilities }

import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N
import org.nlogo.swing.{ QuickHelp, UserAction }, UserAction.{ KeyBindings, MenuAction }

trait QuickHelpAction {
  protected def doHelp(editor: AbstractEditorArea, component: Component): Unit = {
    editor.getTokenAtCaret.foreach(QuickHelp.doHelp(component, _))
  }
}

object QuickHelpAction {
  private val keyboardAction = new KeyboardQuickHelpAction

  def keyboardQuickHelp: KeyboardQuickHelpAction =
    keyboardAction
}

class MouseQuickHelpAction(protected val colorizer: Colorizer)
  extends AbstractAction(I18N.gui.get("tabs.code.rightclick.quickhelp"))
  with EditorAwareAction
  with QuickHelpAction
  with MenuAction {

  accelerator = KeyBindings.keystroke(KeyEvent.VK_F1)

  override def actionPerformed(e: ActionEvent): Unit = {
    doHelp(editor, SwingUtilities.getWindowAncestor(editor))
  }
}

class KeyboardQuickHelpAction
  extends AbstractAction(I18N.gui.get("menu.help.lookUpInDictionary"))
  with QuickHelpAction
  with MenuAction {

  category    = UserAction.HelpCategory
  group       = UserAction.HelpContextGroup
  accelerator = KeyBindings.keystroke(KeyEvent.VK_F1)

  override def actionPerformed(e: ActionEvent): Unit = {
    KeyboardFocusManager.getCurrentKeyboardFocusManager.getPermanentFocusOwner match {
      case editor: AbstractEditorArea =>
        doHelp(editor, Hierarchy.getFrame(editor))

      case _ =>
    }
  }
}
