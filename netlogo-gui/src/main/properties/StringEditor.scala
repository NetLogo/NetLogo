// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.JLabel

import org.nlogo.swing.TextField
import org.nlogo.swing.Implicits._
import org.nlogo.theme.InterfaceColors

abstract class StringEditor(accessor: PropertyAccessor[String], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip)
{
  val editor = new TextField(12) {
    setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setForeground(InterfaceColors.TOOLBAR_TEXT)
    setCaretColor(InterfaceColors.TOOLBAR_TEXT)
  }
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  val label = new JLabel(accessor.displayName)
  label.setForeground(InterfaceColors.DIALOG_TEXT)
  tooltipFont(label)
  add(label, BorderLayout.WEST)
  editor.getDocument.addDocumentListener({ () => changed })
  add(editor, BorderLayout.CENTER)
  override def get = Option(editor.getText)
  override def set(value: String) { editor.setText(value) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }
}
