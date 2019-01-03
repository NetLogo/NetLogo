// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.URI
import javax.swing.JOptionPane

import org.nlogo.api.ExtensionManager
import org.nlogo.core.I18N
import org.nlogo.swing.BrowserLauncher.openURI
import org.nlogo.window.Events.CompiledEvent

// A helpful personal assistant who listens for "extension not found" compiler errors and offers to
// take you to the extension download page.

class ExtensionAssistant(parent: java.awt.Component)
extends CompiledEvent.Handler {

  val ExtensionsURI =
    new URI("https://github.com/NetLogo/NetLogo/wiki/Extensions")

  def handle(e: CompiledEvent) {
    if (isTrigger(e) && confirmOpen())
      openURI(parent, ExtensionsURI)
  }

  def confirmOpen(): Boolean =
    0 == JOptionPane.showConfirmDialog(parent,
      I18N.gui.get("tabs.code.extension.notfound.message"),
      I18N.gui.get("tabs.code.extension.notfound.title"),
      javax.swing.JOptionPane.YES_NO_OPTION)

  def isTrigger(e: CompiledEvent): Boolean =
    Option(e.error).exists(_.getMessage.startsWith(ExtensionManager.extensionNotFoundStr))

}
