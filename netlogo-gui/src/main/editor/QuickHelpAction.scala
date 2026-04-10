// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, SwingUtilities }

import org.nlogo.core.I18N
import org.nlogo.swing.UserAction.MenuAction

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

  override def actionPerformed(e: ActionEvent): Unit = {
    doHelp(editor, SwingUtilities.getWindowAncestor(editor))
  }
}
