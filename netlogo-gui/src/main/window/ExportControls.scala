// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.awt.EventQueue
import org.nlogo.core.LocaleContext
import org.nlogo.swing.OptionPane

object ExportControls {
  def displayExportError(parentComponent: Component, message: String, title: String = loc"Error"): Unit =
    EventQueue.invokeLater(() =>
        new OptionPane(parentComponent, title, message, OptionPane.Options.Ok, OptionPane.Icons.error))
}
