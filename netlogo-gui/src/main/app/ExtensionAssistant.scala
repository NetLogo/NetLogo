// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component

import org.nlogo.api.{ ExtensionManager, LibraryInfoDownloader }
import org.nlogo.app.common.Events.OpenLibrariesDialogEvent
import org.nlogo.core.LocaleContext
import org.nlogo.swing.OptionPane
import org.nlogo.window.Events.CompiledEvent

// A helpful personal assistant who listens for "extension not found" compiler errors and offers to
// help you install them.

class ExtensionAssistant( parent: Component
                        , extExists: (String) => Boolean
                        , lookupExtVersion: (String) => String
                        , installExt: (String, String) => Unit
                        ) extends CompiledEvent.Handler {

  def handle(e: CompiledEvent): Unit = {
    if (LibraryInfoDownloader.enabled && Option(e.error).exists(_.getMessage.startsWith(ExtensionManager.extensionNotFoundStr))) {
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

  def confirmInstall(extName: String, extVersion: String): Boolean = {
    val message = loc"""|You do not have the "$extName" extension installed, but NetLogo can download it for you.\n
                        |Would you like NetLogo to automatically install "$extName" version $extVersion?"""

    new OptionPane(parent, loc"Install Extension?", message, OptionPane.Options.YesNo, OptionPane.Icons.question)
      .getSelectedIndex == 0
  }

  def confirmOpen(extName: String): Boolean = {
    val message = loc"""|Unknown extension: $extName\n\nWould you like to look for extensions in the Extension
                        |Manager?\n\n(If your desired extension is not listed in the Extension Manager, you will need
                        |to manually download and install the extension in order to use it. See the "Extensions Guide"
                        |page of the NetLogo User Manual for more information.)"""

    new OptionPane(parent, loc"Extension not found", message, OptionPane.Options.YesNo, OptionPane.Icons.question)
      .getSelectedIndex == 0
  }

}
