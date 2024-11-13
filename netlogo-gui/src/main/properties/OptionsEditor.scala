// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.FlowLayout
import javax.swing.JLabel

import org.nlogo.api.Options
import org.nlogo.swing.ComboBox
import org.nlogo.theme.InterfaceColors

abstract class OptionsEditor[T](accessor: PropertyAccessor[Options[T]]) extends PropertyEditor(accessor) {
  private val combo = new ComboBox[String]
  setLayout(new FlowLayout(FlowLayout.LEFT))
  private val label = new JLabel(accessor.displayName)
  add(label)
  add(combo)
  private val options: Options[T] = accessor.get
  combo.setItems(options.names)
  private val originalOption: T = options.chosenValue
  combo.addItemListener(_ => changed())
  override def get = {
    options.selectByName(combo.getSelectedItem)
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

  def syncTheme() {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    combo.syncTheme()
  }
}
