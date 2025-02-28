// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component
import javax.swing.{ JComponent, JPanel }

import org.nlogo.awt.RowLayout

// handy for putting rows of buttons at the bottom of dialogs

class ButtonPanel(buttons: Seq[JComponent])
  extends JPanel(new RowLayout(10, Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT)) with Transparent {

  // obey platform standards
  if (System.getProperty("os.name").contains("Mac"))
    buttons.reverse.foreach(add)
  else
    buttons.foreach(add)
}
