// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ BorderLayout, Dimension, Font }
import java.awt.event.{ TextEvent, TextListener }
import javax.swing.{ BorderFactory, JPanel, JScrollPane, ScrollPaneConstants }

import org.nlogo.core.{ CompilerException, I18N, TokenType }
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.editor.{ Colorizer, EditorArea }
import org.nlogo.window.EditorAreaErrorLabel

class AggregateProceduresTab(colorizer: Colorizer) extends JPanel(new BorderLayout) {

  val text = new EditorArea(
    75, 100,
    new Font(platformMonospacedFont, Font.PLAIN, 12),
    true,
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
