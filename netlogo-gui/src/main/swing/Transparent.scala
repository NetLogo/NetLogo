// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JComponent

import org.nlogo.theme.InterfaceColors

trait Transparent extends JComponent {
  setOpaque(false)
  setBackground(InterfaceColors.Transparent)
}
