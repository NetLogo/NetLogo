// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JPanel

import org.nlogo.core.I18N
import org.nlogo.swing.Transparent
import org.nlogo.theme.ThemeSync

import scala.util.{ Failure, Try }

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T], val handlesOwnErrors: Boolean = false)
  extends JPanel with Transparent with ThemeSync {

  protected val defaultError = Failure(new Exception(I18N.gui.getN("edit.general.invalidValue", accessor.name)))

  val originalValue: T = accessor.getter()

  def revert(): Unit = { accessor.setter(Option(originalValue)) }
  def refresh(): Unit = { set(accessor.getter()) }
  def apply(): Unit = { accessor.setter(get.toOption) }

  def get: Try[T]
  def set(value: T): Unit

  def changed: Boolean = !get.toOption.contains(originalValue)
}
