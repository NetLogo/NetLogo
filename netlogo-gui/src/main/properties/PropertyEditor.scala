// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.font.TextAttribute

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T],
                                 val useTooltip: Boolean,
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

  def setTooltip(text: String) = setToolTipText(text)
  def tooltipFont(component: java.awt.Component) =
    if (useTooltip)
      component.setFont(component.getFont.deriveFont(
                        java.util.Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)))
}
