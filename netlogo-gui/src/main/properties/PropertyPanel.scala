// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

abstract class PropertyPanel extends javax.swing.JPanel
{
  val BORDER_PADDING = 5

  def getConstraints = {
    val c = new java.awt.GridBagConstraints
    c.anchor = java.awt.GridBagConstraints.WEST
    c.insets = new java.awt.Insets(3, 3, 3, 3)
    c
  }

  def setTooltip(text: String) = setToolTipText(text)
  def tooltipFont(component: java.awt.Component) =
    if (useTooltip)
      component.setFont(component.getFont.deriveFont(
                        java.util.Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)))
}
