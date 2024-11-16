// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ AbstractAction, JButton, JPanel }
import java.awt.BorderLayout
import java.awt.event.ActionEvent

import org.nlogo.core.I18N
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.log.LogManager
import org.nlogo.swing.InputOptionPane

class CommentableError(val editorArea: AbstractEditorArea) {
  private val errorLabel = new EditorAreaErrorLabel(editorArea)
  private val commentAction = new AbstractAction(I18N.gui.get("tools.loggingMode.errorComment.button")) {
    def actionPerformed(e: ActionEvent) {
      val comment = new InputOptionPane(null, "", I18N.gui.get("tools.loggingMode.errorComment.label")).getInput
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
