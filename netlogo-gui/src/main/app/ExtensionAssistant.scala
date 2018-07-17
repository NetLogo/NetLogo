// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import javax.swing.JOptionPane

import org.nlogo.app.common.Events.OpenLibrariesDialogEvent
import org.nlogo.core.I18N
import org.nlogo.window.Events.CompiledEvent
import org.nlogo.workspace.ExtensionManager

// A helpful personal assistant who listens for "extension not found" compiler errors and offers to
// take you to the extension download page.

class ExtensionAssistant(parent: Component)
extends CompiledEvent.Handler {
  def handle(e: CompiledEvent) =
    if (isTrigger(e) && confirmOpen())
      new OpenLibrariesDialogEvent().raise(parent)

  def confirmOpen(): Boolean =
    0 == JOptionPane.showConfirmDialog(parent,
      I18N.gui.get("tabs.code.extension.notfound.message"),
      I18N.gui.get("tabs.code.extension.notfound.title"),
      JOptionPane.YES_NO_OPTION)

  def isTrigger(e: CompiledEvent): Boolean =
    Option(e.error).exists(_.getMessage.startsWith(
      ExtensionManager.EXTENSION_NOT_FOUND))
}
