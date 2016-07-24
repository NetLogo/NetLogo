// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.{ActionEvent, MouseEvent}
import java.awt.Point
import javax.swing.{AbstractAction, Action}

import org.nlogo.core.I18N
import org.nlogo.editor.EditorArea

class JumpToDeclarationAction() extends AbstractAction {
  putValue(Action.NAME, I18N.gui.get("tabs.code.rightclick.jumptodeclaration"))
  def actionPerformed(e: ActionEvent): Unit = {
    val editorArea = getValue("editor").asInstanceOf[EditorArea]
    val cursorLocation = getValue("cursorLocation").asInstanceOf[Int]
    JumpToDeclaration.jumpToDeclaration(cursorLocation, editorArea)
  }
}
