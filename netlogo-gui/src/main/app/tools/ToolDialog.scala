// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import javax.swing.JDialog

import org.nlogo.core.I18N
import org.nlogo.swing.Implicits.{ thunk2action, thunk2windowAdapter }
import org.nlogo.swing.Positioning
import org.nlogo.swing.Utils.addEscKeyAction

/** ToolDialog provides functionality common to NetLogo tools. */
abstract class ToolDialog(frame: Frame, i18nKey: String)
extends JDialog(frame, I18N.gui.get(s"tools.$i18nKey"), false) {
  /** Makes it easy for tools to access their own i18n keys with I18N.gui(...) */
  protected implicit val i18nPrefix = I18N.Prefix(s"tools.$i18nKey")

  initGUI()
  addWindowListener(() => onClose())
  addEscKeyAction(this, () => {
    onClose()
    setVisible(false)
  })
  Positioning.center(this, frame)

  /** GUI initialization */
  protected def initGUI(): Unit
  protected def onClose(): Unit = {}
}
