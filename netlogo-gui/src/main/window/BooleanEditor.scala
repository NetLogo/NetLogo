// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout
import javax.swing.JPanel

import org.nlogo.swing.{ CheckBox, PreferredSize, Transparent }
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class BooleanEditor(accessor: PropertyAccessor[Boolean])
  extends JPanel with Transparent with PropertyEditor(accessor) with PreferredSize {

  private val checkbox = new CheckBox(accessor.name) {
    addItemListener(_ => accessor.changed())
  }

  setLayout(new BorderLayout(0, 0))

  add(checkbox, BorderLayout.CENTER)

  override def get: Try[Boolean] = Success(checkbox.isSelected)
  override def set(value: Boolean): Unit = { checkbox.setSelected(value) }

  override def requestFocus(): Unit = { checkbox.requestFocus() }
  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    checkbox.setEnabled(enabled)
  }

  override def setToolTipText(text: String): Unit = {
    checkbox.setToolTipText(text)
  }

  override def syncTheme(): Unit = {
    checkbox.setForeground(InterfaceColors.dialogText())
  }
}
