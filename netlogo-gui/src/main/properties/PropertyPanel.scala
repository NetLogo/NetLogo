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
}
