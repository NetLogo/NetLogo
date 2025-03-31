// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JPanel

import org.nlogo.swing.Transparent
import org.nlogo.theme.ThemeSync

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T], val handlesOwnErrors: Boolean = false)
  extends JPanel with Transparent with ThemeSync {

  protected val originalValue: T = accessor.getter()

  def revert() { accessor.setter(originalValue) }
  def refresh() { set(accessor.getter()) }
  def apply() { get.foreach(accessor.setter) }

  def get: Option[T]
  def set(value: T): Unit
}
