// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.api.Options
import org.nlogo.theme.InterfaceColors
import org.nlogo.swing.{ CheckBox, ComboBox }

import scala.util.{ Success, Try }

class InputBoxEditor[InputType <: InputBox#InputType](accessor: PropertyAccessor[Options[InputType]])
  extends PropertyEditor(accessor) {

  private val options: Options[InputType] = accessor.getter()
  private val originalOption: InputType = accessor.getter().chosenValue
  private val originalMultiline: Boolean = originalOption.multiline

  private val label = new JLabel(accessor.name)
  private val typeCombo = new ComboBox[InputType](options.values) {
    setSelectedItem(originalOption)

    addItemListener(_ => {
      apply()

      multiline.setEnabled(selected.map(_.enableMultiline).getOrElse(false))
    })
  }

  private val multiline = new CheckBox("Multi-Line") {
    setSelected(originalMultiline)
  }

  add(label)
  add(typeCombo)
  add(multiline)

  private def selected: Option[InputType] = typeCombo.getSelectedItem

  override def set(value: Options[InputType]): Unit = {
    val t: InputType = value.chosenValue
    typeCombo.setSelectedItem(t)
    multiline.setEnabled(t.enableMultiline)
    multiline.setSelected(t.multiline)
  }

  override def get: Try[Options[InputType]] = {
    options.selectByName(selected.map(_.displayName).getOrElse(""))
    options.chosenValue.multiline(multiline.isSelected)
    Success(options)
  }

  override def revert(): Unit = {
    originalOption.multiline(originalMultiline)
    options.selectValue(originalOption)
    super.revert()
  }

  override def changed: Boolean =
    !selected.contains(originalOption) || multiline.isSelected != originalMultiline

  override def requestFocus(): Unit = { typeCombo.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())
    multiline.setForeground(InterfaceColors.dialogText())

    typeCombo.syncTheme()
  }
}
