// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.Point
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action }

import org.nlogo.editor.EditorAwareAction

class ShowUsageBoxAction(showUsageBox: ShowUsageBox) extends AbstractAction with EditorAwareAction {
  putValue(Action.NAME, "Show Usage")

  def actionPerformed(e: ActionEvent): Unit = {
    showUsageBox.init(editor)
    showUsageBox.showBox(location, documentOffset)
  }
}
