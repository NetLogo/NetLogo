// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

class Label(text: String, useTooltip: Boolean)
  extends PropertyPanel(useTooltip)
{
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  val label = new javax.swing.JLabel(text)
  tooltipFont(label)
  label.setFont(label.getFont.deriveFont(java.awt.Font.BOLD))
  add(label, java.awt.BorderLayout.CENTER)
}
