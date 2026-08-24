// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.{ Box, BoxLayout, JLabel }

import org.nlogo.api.Options
import org.nlogo.swing.{ ComboBox, HorizontalStrut, PreferredSize }
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class OptionsEditor[T](accessor: PropertyAccessor[Options[T]]) extends PropertyEditor(accessor) {
  private val options: Options[T] = accessor.getter()
  private val originalOption: T = options.chosenValue

  private val label = new JLabel(accessor.name)
  private val combo = new ComboBox[String](options.names) with PreferredSize {
    addItemListener(_ => accessor.changed())
  }

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(label)
  add(new HorizontalStrut(6))
  add(combo)
  add(Box.createHorizontalGlue)

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

  override def getMaximumSize: Dimension =
    new Dimension(super.getMaximumSize.width, getPreferredSize.height)

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    combo.syncTheme()
  }
}
