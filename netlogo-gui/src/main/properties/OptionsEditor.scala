// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.FlowLayout
import javax.swing.{ JComboBox, JLabel }

import org.nlogo.api.Options
import org.nlogo.theme.InterfaceColors

abstract class OptionsEditor[T](accessor: PropertyAccessor[Options[T]], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  private val combo = new JComboBox[String]
  setLayout(new FlowLayout(FlowLayout.LEFT))
  private val label = new JLabel(accessor.displayName)
  tooltipFont(label)
  add(label)
  add(combo)
  private val options: Options[T] = accessor.get
  for(optionName <- options.names)
    combo.addItem(optionName)
  private val originalOption: T = options.chosenValue
  combo.addActionListener(_ => changed())
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

  def syncTheme() {
    label.setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
