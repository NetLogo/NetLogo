// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import
  javax.swing.JOptionPane,
  java.net.URI,
  org.nlogo.workspace.ExtensionManager,
  org.nlogo.window.Events.CompiledEvent,
  org.nlogo.swing.BrowserLauncher.openURI

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
      "Go to download site for NetLogo extensions?",
      "Extension not found",
      javax.swing.JOptionPane.YES_NO_OPTION)

  def isTrigger(e: CompiledEvent): Boolean =
    Option(e.error).exists(_.getMessage.startsWith(
      ExtensionManager.EXTENSION_NOT_FOUND))

}
