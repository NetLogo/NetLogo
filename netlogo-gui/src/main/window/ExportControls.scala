// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.OptionPane

object ExportControls {
  def displayExportError(parentComponent: Component, message: String, title: String = I18N.gui.get("common.messages.error")): Unit =
    EventQueue.invokeLater(() =>
        new OptionPane(parentComponent, title, message, OptionPane.Options.Ok, OptionPane.Icons.Error))
}
