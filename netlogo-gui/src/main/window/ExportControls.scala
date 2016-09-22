// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import javax.swing.JOptionPane

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.Implicits.thunk2runnable

object ExportControls {
  def displayExportError(parentComponent: Component, message: String, title: String = I18N.gui.get("common.messages.error")): Unit =
    EventQueue.invokeLater(() =>
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE))
}
