// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout

import org.nlogo.swing.CheckBox
import org.nlogo.theme.InterfaceColors

class BooleanEditor(accessor: PropertyAccessor[Boolean]) extends PropertyEditor(accessor) {
  private val checkbox = new CheckBox(accessor.name, _ => accessor.changed())

  setLayout(new BorderLayout(0, 0))

  add(checkbox, BorderLayout.CENTER)

  override def get: Option[Boolean] = Some(checkbox.isSelected)
  override def set(value: Boolean): Unit = { checkbox.setSelected(value) }

  override def requestFocus() { checkbox.requestFocus() }
  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    checkbox.setEnabled(enabled)
  }

  override def setTooltip(text: String): Unit = { checkbox.setToolTipText(text) }

  override def syncTheme(): Unit = {
    checkbox.setForeground(InterfaceColors.dialogText())
  }
}
