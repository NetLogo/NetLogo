// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.Options
import org.nlogo.swing.Implicits._

abstract class OptionsEditor[T](accessor: PropertyAccessor[Options[T]])
  extends PropertyEditor(accessor)
{
  private val combo = new javax.swing.JComboBox[String]
  setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
  add(new javax.swing.JLabel(accessor.displayName))
  add(combo)
  private val options: Options[T] = accessor.get
  for(optionName <- options.names)
    combo.addItem(optionName)
  private val originalOption: T = options.chosenValue
  combo.addActionListener(changed _)
  override def get = {
    options.selectByName(combo.getSelectedItem.asInstanceOf[String])
    Some(options)
  }
  override def set(value: Options[T]) {
    combo.setSelectedItem(value.chosenName)
  }
  override def revert() {
    options.selectValue(originalOption)
    super.revert()
  }
  override def requestFocus() {
    combo.requestFocus()
  }
}
