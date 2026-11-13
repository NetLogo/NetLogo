// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import javax.swing.JDialog

import org.nlogo.swing.{ Positioning, ZoomableWindow }
import org.nlogo.swing.Implicits.{ thunk2action, thunk2windowAdapter }
import org.nlogo.swing.Utils.addEscKeyAction

/** ToolDialog provides functionality common to NetLogo tools. */
abstract class ToolDialog(frame: Frame, title: String)
  extends JDialog(frame, title, false) with ZoomableWindow(Option(frame)) {

  addWindowListener(() => onClose())
  addEscKeyAction(this, () => {
    onClose()
    setVisible(false)
  })

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Positioning.center(this, frame)

    super.setVisible(visible)
  }

  protected def onClose(): Unit = {}
}
