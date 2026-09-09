// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import javax.swing.JDialog

import org.nlogo.core.I18N
import org.nlogo.swing.{ Positioning, ZoomableWindow }
import org.nlogo.swing.Implicits.{ thunk2action, thunk2windowAdapter }
import org.nlogo.swing.Utils.addEscKeyAction

/** ToolDialog provides functionality common to NetLogo tools. */
abstract class ToolDialog(frame: Frame, i18nKey: String)
extends JDialog(frame, I18N.gui.get(s"tools.$i18nKey"), false) with ZoomableWindow(Option(frame)) {
  /** Makes it easy for tools to access their own i18n keys with I18N.gui(...) */
  protected implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix(s"tools.$i18nKey")

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
