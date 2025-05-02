// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JPanel

import org.nlogo.swing.Transparent
import org.nlogo.theme.ThemeSync

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T], val handlesOwnErrors: Boolean = false)
  extends JPanel with Transparent with ThemeSync {

  val originalValue: T = accessor.getter()

  def revert(): Unit = { accessor.setter(originalValue) }
  def refresh(): Unit = { set(accessor.getter()) }
  def apply(): Unit = { get.foreach(accessor.setter) }

  def get: Option[T]
  def set(value: T): Unit
}
