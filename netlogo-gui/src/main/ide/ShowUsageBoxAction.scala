// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.{ActionEvent, MouseEvent}
import java.awt.Point
import javax.swing.{AbstractAction, Action}

import org.nlogo.editor.EditorArea

class ShowUsageBoxAction(showUsageBox: ShowUsageBox) extends AbstractAction {
  putValue(Action.NAME, "Show Usage")
  def actionPerformed(e: ActionEvent): Unit = {
    val editorArea = getValue("editor").asInstanceOf[EditorArea]
    val popupLocation = getValue("popupLocation").asInstanceOf[Point]
    val cursorLocation = getValue("cursorLocation").asInstanceOf[Int]
    showUsageBox.init(editorArea)
    showUsageBox.showBox(popupLocation, cursorLocation)
  }
}
