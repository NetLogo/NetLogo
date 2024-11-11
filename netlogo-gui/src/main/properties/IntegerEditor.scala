// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.JLabel

import org.nlogo.swing.TextField
import org.nlogo.swing.Implicits._
import org.nlogo.theme.InterfaceColors

import util.control.Exception.catching

abstract class IntegerEditor(accessor: PropertyAccessor[Int], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  private val editor = new TextField(8)
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  private val label = new JLabel(accessor.displayName)
  tooltipFont(label)
  add(label, BorderLayout.WEST)
  editor.getDocument().addDocumentListener({ () => changed() })
  add(editor, BorderLayout.CENTER)
  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    editor.setEnabled(enabled)
    label.setEnabled(enabled)
  }
  override def get =
    catching(classOf[NumberFormatException])
      .opt(editor.getText.toInt)
  override def set(value: Int) { editor.setText(value.toString) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.025
    c
  }

  def syncTheme() {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    editor.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    editor.setForeground(InterfaceColors.TOOLBAR_TEXT)
    editor.setCaretColor(InterfaceColors.TOOLBAR_TEXT)
  }
}
