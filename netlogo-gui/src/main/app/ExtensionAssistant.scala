// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import javax.swing.JOptionPane

import org.nlogo.api.ExtensionManager
import org.nlogo.app.common.Events.OpenLibrariesDialogEvent
import org.nlogo.core.I18N
import org.nlogo.window.Events.CompiledEvent

// A helpful personal assistant who listens for "extension not found" compiler errors and offers to
// help you install them.

class ExtensionAssistant( parent: Component
                        , extExists: (String) => Boolean
                        , lookupExtVersion: (String) => String
                        , installExt: (String, String) => Unit
                        ) extends CompiledEvent.Handler {

  def handle(e: CompiledEvent) {
    if (Option(e.error).exists(_.getMessage.startsWith(ExtensionManager.extensionNotFoundStr))) {
      val missingExtName = e.error.getMessage.stripPrefix(ExtensionManager.extensionNotFoundStr)
      if (extExists(missingExtName)) {
        val missingExtVersion = lookupExtVersion(missingExtName)
        if (confirmInstall(missingExtName, missingExtVersion))
          installExt(missingExtName, missingExtVersion)
      } else if (confirmOpen(missingExtName)) {
        new OpenLibrariesDialogEvent().raise(parent)
      }
    }
  }

  def confirmInstall(extName: String, extVersion: String): Boolean =
    0 == JOptionPane.showConfirmDialog(parent,
      I18N.gui.getN("tabs.code.extension.installable.message", extName, extVersion),
      I18N.gui.get("tabs.code.extension.installable.title"),
      javax.swing.JOptionPane.YES_NO_OPTION)

  def confirmOpen(extName: String): Boolean =
    0 == JOptionPane.showConfirmDialog(parent,
      I18N.gui.getN("tabs.code.extension.notfound.message", extName),
      I18N.gui.get("tabs.code.extension.notfound.title"),
      javax.swing.JOptionPane.YES_NO_OPTION)

}
