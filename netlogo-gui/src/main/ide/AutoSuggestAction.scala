// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.ActionEvent
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.TextAction

import org.nlogo.editor.EditorArea

class AutoSuggestAction(name: String, codeCompletionPopup: CodeCompletionPopup) extends TextAction(name) {
  final val autoSuggestDocumentListener = new AutoSuggestDocumentListener(codeCompletionPopup)

  override def actionPerformed(e: ActionEvent): Unit = {
    val editorArea = getTextComponent(e).asInstanceOf[EditorArea]
    codeCompletionPopup.init(editorArea, autoSuggestDocumentListener)

    codeCompletionPopup.displayPopup()

    editorArea.getDocument().removeDocumentListener(autoSuggestDocumentListener)
    editorArea.getDocument().addDocumentListener(autoSuggestDocumentListener)
  }
}
class AutoSuggestDocumentListener(codeCompletionPopup: CodeCompletionPopup) extends DocumentListener {
  override def changedUpdate(e: DocumentEvent): Unit = {
}
  override def insertUpdate(e: DocumentEvent): Unit = {
  codeCompletionPopup.fireUpdatePopup(Some(e))
}
  override def removeUpdate(e: DocumentEvent): Unit = {
  codeCompletionPopup.fireUpdatePopup(Some(e))
}
}
