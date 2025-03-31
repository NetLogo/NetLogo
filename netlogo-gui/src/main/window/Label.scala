// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.theme.InterfaceColors

class Label(accessor: PropertyAccessor[String]) extends PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name)

  setOpaque(true)

  add(label)

  override def get: Option[String] = Some("")
  override def set(value: String): Unit = {}

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.bspaceHintBackground())

    label.setForeground(InterfaceColors.dialogText())
  }
}
