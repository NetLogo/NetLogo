// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout
import javax.swing.JLabel

import org.nlogo.api.Options
import org.nlogo.swing.ComboBox
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class OptionsEditor[T](accessor: PropertyAccessor[Options[T]]) extends PropertyEditor(accessor) {
  private val options: Options[T] = accessor.getter()
  private val originalOption: T = options.chosenValue

  private val label = new JLabel(accessor.name)
  private val combo = new ComboBox[String](options.names) {
    addItemListener(_ => accessor.changed())
  }

  setLayout(new BorderLayout(6, 0))

  add(label, BorderLayout.WEST)
  add(combo, BorderLayout.CENTER)

  override def get: Try[Options[T]] = {
    options.selectByName(combo.getSelectedItem.getOrElse(""))
    Success(options)
  }

  override def set(value: Options[T]): Unit = {
    combo.setSelectedItem(value.chosenName)
  }

  override def revert(): Unit = {
    options.selectValue(originalOption)
    super.revert()
  }

  override def requestFocus(): Unit = {
    combo.requestFocus()
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    combo.syncTheme()
  }
}
