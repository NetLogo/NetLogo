// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }

import org.nlogo.swing.CheckBox
import org.nlogo.theme.InterfaceColors

abstract class BooleanEditor(accessor: PropertyAccessor[Boolean], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  private val checkbox = new CheckBox(accessor.displayName)
  tooltipFont(checkbox)
  checkbox.addItemListener(_ => changed())
  setLayout(new BorderLayout)
  add(checkbox, BorderLayout.CENTER)
  override def get = Some(checkbox.isSelected)
  override def set(value: Boolean) { checkbox.setSelected(value) }
  override def requestFocus() { checkbox.requestFocus() }
  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    checkbox.setEnabled(enabled)
  }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c
  }
  override def setTooltip(text: String) = checkbox.setToolTipText(text)

  def syncTheme() {
    checkbox.setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
