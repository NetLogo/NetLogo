// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.FlowLayout
import javax.swing.{ JCheckBox, JComboBox, JLabel }

import org.nlogo.api.Options
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.InputBox

abstract class InputBoxEditor(accessor: PropertyAccessor[Options[InputBox#InputType]], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  private val typeCombo: JComboBox[InputBox#InputType] = new JComboBox[InputBox#InputType]
  private val multiline: JCheckBox = new JCheckBox("Multi-Line")
  private val options: Options[InputBox#InputType] = accessor.get
  private val originalOption: InputBox#InputType = accessor.get.chosenValue
  private val originalMultiline: Boolean = accessor.get.chosenValue.multiline

  setLayout(new FlowLayout(FlowLayout.LEFT))
  private val label = new JLabel(accessor.displayName)
  tooltipFont(label)
  add(label)
  add(typeCombo)

  for (t <- options.values)
    typeCombo.addItem(t)
  typeCombo.addActionListener(_ => multiline.setEnabled(selected.enableMultiline))

  multiline.setSelected(originalOption.multiline)
  add(multiline)

  private def selected = typeCombo.getSelectedItem.asInstanceOf[InputBox#InputType]

  override def set(value: Options[InputBox#InputType]): Unit = {
    val t: InputBox#InputType = value.chosenValue
    typeCombo.setSelectedItem(t)
    multiline.setEnabled(t.enableMultiline)
    multiline.setSelected(t.multiline)
  }

  override def get = {
    options.selectByName(selected.displayName)
    options.chosenValue.multiline(multiline.isSelected)
    Some(options)
  }

  override def revert() {
    originalOption.multiline(originalMultiline)
    options.selectValue(originalOption)
    super.revert
  }

  override def requestFocus = typeCombo.requestFocus

  def syncTheme() {
    label.setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
