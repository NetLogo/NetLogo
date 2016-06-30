// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.core.{ CompilerException, TokenType }
import org.nlogo.editor.Colorizer

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
