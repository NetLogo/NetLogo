// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ AbstractAction, JButton, JOptionPane, JPanel }
import java.awt.BorderLayout
import java.awt.event.ActionEvent

import org.nlogo.core.I18N
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.log.LogManager

class CommentableError(val editorArea: AbstractEditorArea) {
  private val errorLabel = new EditorAreaErrorLabel(editorArea)
  private val commentAction = new AbstractAction(I18N.gui.get("tools.loggingMode.errorComment.button")) {
    def actionPerformed(e: ActionEvent) {
      val comment = JOptionPane.showInputDialog(null, I18N.gui.get("tools.loggingMode.errorComment.label"), "",
        JOptionPane.QUESTION_MESSAGE, null, null, "").asInstanceOf[String]
      if (comment != null && !comment.trim().isEmpty()) {
        LogManager.userComment(comment)
      }
    }
  }
  private val commentButton = new JButton(commentAction)

  val component: JPanel = new JPanel(new BorderLayout) {
    add(errorLabel, BorderLayout.CENTER)
  }

  def setError(compilerError: Exception, offset: Int): Unit = {
    component.setVisible(compilerError != null)

    if (LogManager.isStarted) {
      component.add(commentButton, BorderLayout.WEST)
    } else {
      component.remove(commentButton)
    }

    errorLabel.setError(compilerError, offset)
  }

  def zoom(zoomFactor: Double) {
    errorLabel.zoom(zoomFactor)
  }

  def setVisible(isVisible: Boolean) {
    component.setVisible(isVisible)
  }

  def setText(text: String) {
    errorLabel.setText(text)
  }

}
