// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component
import javax.swing.{ JButton, JPanel, JComponent }

import org.nlogo.awt.RowLayout
import org.nlogo.theme.InterfaceColors

// handy for putting rows of buttons at the bottom of dialogs

object ButtonPanel {
  def apply(buttons: JComponent*) = new ButtonPanel(buttons.toArray)
  def apply(buttons: List[JButton]) = new ButtonPanel(buttons.toArray)
}

class ButtonPanel(buttons: Array[JComponent])
  extends JPanel(new RowLayout(10, Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT)) {

  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  buttons.foreach(add)
}
