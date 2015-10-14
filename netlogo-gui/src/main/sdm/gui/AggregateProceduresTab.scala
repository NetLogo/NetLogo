// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.{CompilerException, TokenType}
import org.nlogo.editor.Colorizer

class AggregateProceduresTab(colorizer: Colorizer[TokenType]) extends javax.swing.JPanel {
  val text = new org.nlogo.editor.EditorArea[TokenType](
    50, 75,
    new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont,
                      java.awt.Font.PLAIN, 12),
    true,
    // Dummy listener since the editor is not editable
    new java.awt.event.TextListener() {
        override def textValueChanged(e: java.awt.event.TextEvent) { } },
    colorizer,
    org.nlogo.api.I18N.gui.get _)
  text.setBorder(
    javax.swing.BorderFactory.createEmptyBorder(4, 7, 4, 7))
  text.setEditable(false)
  setLayout(new java.awt.BorderLayout())
  add(new javax.swing.JScrollPane
      (text,
       javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
       javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS),
      java.awt.BorderLayout.CENTER)
  text.scrollRectToVisible(new java.awt.Rectangle(1, 1, 1, 1))
  def setError(e: CompilerException) {
    if(e != null) {
      text.select(e.startPos, e.endPos)
      text.requestFocus()
    }
  }
  def setText(text: String) {
    this.text.setText(text)
    this.text.scrollRectToVisible(new java.awt.Rectangle(1, 1, 1, 1))
  }
}
