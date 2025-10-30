// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Window
import java.awt.event.{ WindowAdapter, WindowEvent }

object AutomateWindow {
  private var automated = false

  def setAutomated(automated: Boolean): Unit = {
    this.automated = automated
  }
}

// helper for GUI testing, prevents windows from blocking regardless of modality (Isaac B 10/29/25)
trait AutomateWindow extends Window {
  if (AutomateWindow.automated) {
    addWindowFocusListener(new WindowAdapter {
      override def windowGainedFocus(e: WindowEvent): Unit = {
        setVisible(false)
      }
    })
  }
}
