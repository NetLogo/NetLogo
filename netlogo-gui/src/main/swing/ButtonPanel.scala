// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component

// handy for putting rows of buttons at the bottom of dialogs

object ButtonPanel {
  // obey platform standards
  private def platformButtons(buttons: Seq[Component]): Seq[Component] = {
    if (System.getProperty("os.name").contains("Mac")) {
      buttons.reverse
    } else {
      buttons
    }
  }
}

class ButtonPanel(buttons: Seq[Component])
  extends BoxRow(ButtonPanel.platformButtons(buttons), 10, BoxAlign.Center) with MaximumHeight
