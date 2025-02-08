// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.FlowLayout
import javax.swing.JLabel

import org.nlogo.api.Options
import org.nlogo.theme.InterfaceColors
import org.nlogo.swing.{ CheckBox, ComboBox }
import org.nlogo.window.InputBox

abstract class InputBoxEditor(accessor: PropertyAccessor[Options[InputBox#InputType]])
  extends PropertyEditor(accessor) {

  private val options: Options[InputBox#InputType] = accessor.get
  private val typeCombo = new ComboBox[InputBox#InputType](options.values)
  private val multiline = new CheckBox("Multi-Line")
  private val originalOption: InputBox#InputType = accessor.get.chosenValue
  private val originalMultiline: Boolean = accessor.get.chosenValue.multiline

  setLayout(new FlowLayout(FlowLayout.LEFT))
  private val label = new JLabel(accessor.displayName)
  add(label)
  add(typeCombo)

  typeCombo.addItemListener(_ => multiline.setEnabled(selected.map(_.enableMultiline).getOrElse(false)))

  multiline.setSelected(originalOption.multiline)
  add(multiline)

  private def selected = typeCombo.getSelectedItem

  override def set(value: Options[InputBox#InputType]): Unit = {
    val t: InputBox#InputType = value.chosenValue
    typeCombo.setSelectedItem(t)
    multiline.setEnabled(t.enableMultiline)
    multiline.setSelected(t.multiline)
  }

  override def get = {
    options.selectByName(selected.map(_.displayName).getOrElse(""))
    options.chosenValue.multiline(multiline.isSelected)
    Some(options)
  }

  override def revert() {
    originalOption.multiline(originalMultiline)
    options.selectValue(originalOption)
    super.revert
  }

  override def requestFocus = typeCombo.requestFocus

  def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.DIALOG_TEXT)
    multiline.setForeground(InterfaceColors.DIALOG_TEXT)

    typeCombo.syncTheme()
  }
}
