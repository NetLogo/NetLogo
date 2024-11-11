// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.JLabel

import org.nlogo.theme.InterfaceColors

abstract class Label(accessor: PropertyAccessor[String], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip) {

  setLayout(new BorderLayout(BORDER_PADDING, 0))
  setOpaque(true)
  private val label = new JLabel(accessor.displayName)
  tooltipFont(label)
  add(label, BorderLayout.CENTER)
  override def get: Option[String] = Some("")
  override def set(value: String) { }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c
  }

  def syncTheme() {
    setBackground(InterfaceColors.BSPACE_HINT_BACKGROUND)

    label.setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
