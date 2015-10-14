// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import org.nlogo.awt.RowLayout
import java.awt.Component._
import javax.swing.{JButton, BorderFactory, JPanel, JComponent}

// handy for putting rows of buttons at the bottom of dialogs,
// and having them reverse order on Macs

// Mac is Cancel OK, Windows is OK Cancel - JC 4/7/10
object ButtonPanel {
  val MAC = System.getProperty("os.name").startsWith("Mac")
  def apply(buttons: JComponent*) = new ButtonPanel(buttons.toArray)
  def apply(buttons: List[JButton]) = new ButtonPanel(buttons.toArray)
}

class ButtonPanel(buttons: Array[JComponent]) extends JPanel {
  import ButtonPanel.MAC
  if (MAC) {
    // avoid grow box in lower right corner of dialog
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
  }
  setLayout(new RowLayout(10, if (MAC) RIGHT_ALIGNMENT else CENTER_ALIGNMENT, CENTER_ALIGNMENT))

  (if (MAC) buttons.reverse else buttons).foreach(add)
}
