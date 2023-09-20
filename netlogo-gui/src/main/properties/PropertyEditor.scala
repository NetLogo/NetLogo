// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T],
                                 val handlesOwnErrors: Boolean = false)
         extends PropertyPanel
{

  def changed() // abstract

  private val originalValue: T = accessor.get

  def revert() { accessor.set(originalValue) }
  def refresh() { set(accessor.get) }

  def get: Option[T]
  def set(value: T): Unit
  def apply() { get.foreach(accessor.set) }
}
