// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.core.{ CompilerException, TokenType, I18N }
import org.nlogo.editor.Colorizer
import org.nlogo.window.EditorAreaErrorLabel
import java.awt.event.TextListener
import java.awt.event.TextEvent
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.JPanel

class AggregateProceduresTab(colorizer: Colorizer) extends javax.swing.JPanel {
  val text = new org.nlogo.editor.EditorArea(
    50, 75,
    new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont,
                      java.awt.Font.PLAIN, 12),
    false,
    // Dummy listener since the editor is not editable
    new TextListener() { override def textValueChanged(e: TextEvent) { } },
    colorizer, I18N.gui.get _)

  private val errorLabel = new EditorAreaErrorLabel(text)

  text.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7))
  text.setEditable(false)

  val scrollableEditor = new JScrollPane(
    text,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  val codePanel = new JPanel(new BorderLayout) {
    add(scrollableEditor, BorderLayout.CENTER)
    add(errorLabel, BorderLayout.NORTH)
  }
  add(codePanel, BorderLayout.CENTER)

  // override def getPreferredSize: Dimension = AggregateModelEditor.WindowSize

  def setError(e: CompilerException, offset: Int) {
    errorLabel.setError(e, offset)
  }

  def clearError(): Unit = {
    errorLabel.setError(null, 0)
  }

  def setText(newText: String) {
    text.setText(newText)
  }
}
