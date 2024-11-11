// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.{ JLabel, JScrollPane, JTextArea, ScrollPaneConstants, SwingConstants }

import org.nlogo.swing.Implicits._
import org.nlogo.theme.InterfaceColors

abstract class BigStringEditor(accessor: PropertyAccessor[String], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  setLayout(new BorderLayout(BORDER_PADDING, 0))
  val label = new JLabel(accessor.displayName)
  label.setForeground(InterfaceColors.DIALOG_TEXT)
  tooltipFont(label)
  label.setVerticalAlignment(SwingConstants.TOP)
  add(label, BorderLayout.NORTH)
  private val editor = new JTextArea(6, 30)
  editor.setDragEnabled(false)
  editor.setLineWrap(true)
  editor.setWrapStyleWord(true)
  editor.getDocument().addDocumentListener({ () => changed() })
  editor.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
  editor.setCaretColor(InterfaceColors.TOOLBAR_TEXT)
  add(new JScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER)
  override def get = Option(editor.getText())
  override def set(value: String) {
    editor.setText(value)
    editor.select(0, 0)
  }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1.0
    c.weighty = 1.0
    c
  }
}
