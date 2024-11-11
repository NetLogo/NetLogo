// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ GridBagConstraints, Insets }
import javax.swing.JPanel

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T], val handlesOwnErrors: Boolean = false)
  extends JPanel with ThemeSync {

  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  def changed() // abstract

  val BORDER_PADDING = 5

  private val originalValue: T = accessor.get

  def revert() { accessor.set(originalValue) }
  def refresh() { set(accessor.get) }

  def get: Option[T]
  def set(value: T): Unit
  def apply() { get.foreach(accessor.set) }

  def getConstraints = {
    val c = new GridBagConstraints
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(3, 3, 3, 3)
    c
  }

  def setTooltip(text: String) = setToolTipText(text)
}
