// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ GridBagConstraints }

abstract class BooleanEditor(accessor: PropertyAccessor[Boolean])
  extends PropertyEditor(accessor)
{
  private val checkbox = new javax.swing.JCheckBox
  checkbox.setText(accessor.displayName)
  checkbox.addItemListener(_ => changed())
  setLayout(new java.awt.BorderLayout)
  add(checkbox, java.awt.BorderLayout.CENTER)
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
}
