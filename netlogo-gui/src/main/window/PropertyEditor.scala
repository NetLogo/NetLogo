// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.core.LocaleContext
import org.nlogo.theme.ThemeSync

import scala.util.{ Failure, Try }

trait PropertyEditor[T](val accessor: PropertyAccessor[T], val handlesOwnErrors: Boolean = false)
  extends Component with ThemeSync {

  protected val defaultError = Failure(new Exception(loc"Invalid value for ${accessor.name}"))

  val originalValue: T = accessor.getter()

  def revert(): Unit = { accessor.setter(Option(originalValue)) }
  def refresh(): Unit = { set(accessor.getter()) }
  def apply(): Unit = { accessor.setter(get.toOption) }

  def get: Try[T]
  def set(value: T): Unit

  def changed: Boolean = !get.toOption.contains(originalValue)
}
