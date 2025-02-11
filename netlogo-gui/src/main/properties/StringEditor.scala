// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.JLabel

import org.nlogo.swing.TextField
import org.nlogo.swing.Implicits._
import org.nlogo.theme.InterfaceColors

abstract class StringEditor(accessor: PropertyAccessor[String])
  extends PropertyEditor(accessor) {

  private val editor = new TextField(12)
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  private val label = new JLabel(accessor.displayName)
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

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText)

    editor.syncTheme()
  }
}
