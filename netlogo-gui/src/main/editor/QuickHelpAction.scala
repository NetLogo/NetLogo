// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, SwingUtilities }

import org.nlogo.core.I18N
import org.nlogo.swing.UserAction.{ KeyBindings, MenuAction }

trait QuickHelpAction {
  protected def colorizer: Colorizer

  def doHelp(editor: AbstractEditorArea, component: Component): Unit = {
    editor.getTokenAtCaret.foreach(colorizer.doHelp(component, _))
  }
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
