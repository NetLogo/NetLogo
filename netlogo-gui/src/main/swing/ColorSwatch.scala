// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

class ColorSwatch(width: Int, height: Int)
// JPanel not JComponent otherwise paintComponent() doesn't paint the
// background color for reasons I can't fathom - ST 8/3/03
extends javax.swing.JPanel
{
  override def getPreferredSize =
    new java.awt.Dimension(width, height)
}
