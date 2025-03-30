// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.{ JLabel, ScrollPaneConstants, SwingConstants }
import javax.swing.border.LineBorder

import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ ScrollPane, TextArea }
import org.nlogo.theme.InterfaceColors

abstract class BigStringEditor(accessor: PropertyAccessor[String])
  extends PropertyEditor(accessor) {

  setLayout(new BorderLayout(BORDER_PADDING, 0))
  private val label = new JLabel(accessor.displayName)
  label.setVerticalAlignment(SwingConstants.TOP)
  add(label, BorderLayout.NORTH)
  private val editor = new TextArea(6, 30)
  editor.setDragEnabled(false)
  editor.setLineWrap(true)
  editor.setWrapStyleWord(true)
  editor.getDocument().addDocumentListener({ () => changed() })
  private val scrollPane = new ScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  add(scrollPane, BorderLayout.CENTER)
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

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())
  }
}
