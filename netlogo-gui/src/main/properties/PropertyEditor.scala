// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.font.TextAttribute

abstract class PropertyEditor[T](val accessor: PropertyAccessor[T],
                                 val useTooltip: Boolean,
                                 val handlesOwnErrors: Boolean = false)
         extends javax.swing.JPanel
{

  def changed() // abstract

  val BORDER_PADDING = 5

  private val originalValue: T = accessor.get

  def revert() { accessor.set(originalValue) }
  def refresh() { set(accessor.get) }

  def get: Option[T]
  def set(value: T): Unit
  def apply() { get.foreach(accessor.set) }

  def getConstraints = {
    val c = new java.awt.GridBagConstraints
    c.anchor = java.awt.GridBagConstraints.WEST
    c.insets = new java.awt.Insets(3, 3, 3, 3)
    c
  }

  def tooltipFont(component: java.awt.Component) =
    if (useTooltip)
      component.setFont(component.getFont.deriveFont(
                        java.util.Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)))
}
