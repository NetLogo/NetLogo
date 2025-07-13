// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Window

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.theme.ThemeSync

trait ClientAppInterface extends Window with ThemeSync {
  def startup(userid: String, hostip: String, port: Int, isLocal: Boolean, isRobo: Boolean, waitTime: Long,
              workspace: CompilerServices, extensionManager: ExtensionManager): Unit
}
