// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

abstract class Label(accessor: PropertyAccessor[String], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip)
{
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  val label = new javax.swing.JLabel(accessor.displayName)
  tooltipFont(label)
  add(label, java.awt.BorderLayout.CENTER)
  override def get: Option[String] = Some("")
  override def set(value: String) { }
}
