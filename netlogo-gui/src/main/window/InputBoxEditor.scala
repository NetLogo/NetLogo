// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.api.Options
import org.nlogo.theme.InterfaceColors
import org.nlogo.swing.{ CheckBox, ComboBox }

class InputBoxEditor(accessor: PropertyAccessor[Options[InputBox#InputType]]) extends PropertyEditor(accessor) {
  private val options: Options[InputBox#InputType] = accessor.getter()
  private val originalOption: InputBox#InputType = accessor.getter().chosenValue
  private val originalMultiline: Boolean = originalOption.multiline

  private val label = new JLabel(accessor.name)
  private val typeCombo = new ComboBox[InputBox#InputType](options.values) {
    addItemListener(_ => multiline.setEnabled(selected.map(_.enableMultiline).getOrElse(false)))
  }

  private val multiline = new CheckBox("Multi-Line") {
    setSelected(originalMultiline)
  }

  add(label)
  add(typeCombo)
  add(multiline)

  private def selected: Option[InputBox#InputType] = typeCombo.getSelectedItem

  override def set(value: Options[InputBox#InputType]): Unit = {
    val t: InputBox#InputType = value.chosenValue
    typeCombo.setSelectedItem(t)
    multiline.setEnabled(t.enableMultiline)
    multiline.setSelected(t.multiline)
  }

  override def get: Option[Options[InputBox#InputType]] = {
    options.selectByName(selected.map(_.displayName).getOrElse(""))
    options.chosenValue.multiline(multiline.isSelected)
    Some(options)
  }

  override def revert(): Unit = {
    originalOption.multiline(originalMultiline)
    options.selectValue(originalOption)
    super.revert()
  }

  override def requestFocus(): Unit = { typeCombo.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())
    multiline.setForeground(InterfaceColors.dialogText())

    typeCombo.syncTheme()
  }
}
