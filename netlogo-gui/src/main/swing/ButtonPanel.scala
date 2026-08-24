// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component
import javax.swing.{ Box, BoxLayout, JPanel }

// handy for putting rows of buttons at the bottom of dialogs

class ButtonPanel(buttons: Seq[Component]) extends JPanel with Transparent {
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(Box.createHorizontalGlue)

  locally {
    // obey platform standards
    val ordered: Seq[Component] = {
      if (System.getProperty("os.name").contains("Mac")) {
        buttons.reverse
      } else {
        buttons
      }
    }

    ordered.headOption.foreach(add)

    ordered.drop(1).foreach { button =>
      add(new HorizontalStrut(10))
      add(button)
    }
  }

  add(Box.createHorizontalGlue)
}
