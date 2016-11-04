// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.text.Document

import org.nlogo.core.I18N
import RichDocument._

trait QuickHelpAction {
  def colorizer: Colorizer

  def doHelp(document: Document, offset: Int, component: Component): Unit = {
    if (offset != -1) {
      val lineNumber = document.offsetToLine(offset)
      for {
        lineText    <- document.getLineText(document.offsetToLine(offset))
        tokenString <- colorizer.getTokenAtPosition(lineText, offset - document.lineToStartOffset(lineNumber))
      } {
        colorizer.doHelp(component, tokenString)
      }
    }
  }
}

class MouseQuickHelpAction(val colorizer: Colorizer)
  extends AbstractAction(I18N.gui.get("tabs.code.rightclick.quickhelp"))
  with EditorAwareAction
  with QuickHelpAction {

  override def actionPerformed(e: ActionEvent): Unit = {
    doHelp(editor.getDocument, documentOffset, editor)
  }
}
