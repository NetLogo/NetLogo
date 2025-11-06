// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Window
import java.awt.event.{ WindowAdapter, WindowEvent }

object WindowAutomator {

  private var isAutomating: Boolean     = false
  private var windows:      Seq[Window] = Seq()

  def setAutomated(automated: Boolean): Unit = {
    isAutomating = automated
  }

  // helper for GUI testing, prevents windows from blocking regardless of modality (Isaac B 10/29/25)
  def automate(w: Window): Unit = {
    if (isAutomating) {
      windows = windows :+ w
      w.addWindowFocusListener(
        new WindowAdapter {
          override def windowGainedFocus(e: WindowEvent): Unit = {
            w.setVisible(false)
          }
        }
      )
    }
  }

  def getVisibleWindows: Seq[Window] =
    windows.filter(_.isVisible)

  def resetWindows(): Unit = {
    windows.foreach(_.dispose())

    windows = Seq()
  }

}
