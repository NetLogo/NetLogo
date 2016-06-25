package or.nlogo.ide

import java.awt.event.{ActionEvent, MouseEvent}
import javax.swing.text.TextAction

import org.nlogo.editor.EditorArea
import org.nlogo.ide.ShowUsageBox

class ShowUsageBoxAction(showUsageBox: ShowUsageBox) {
  def actionPerformed(me: MouseEvent): Unit = {
    val editorArea = me.getSource.asInstanceOf[EditorArea]
    showUsageBox.init(editorArea)
    showUsageBox.showBox(me, editorArea.getCaretPosition)
  }
}
