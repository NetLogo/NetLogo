// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ ExtensionManager => ApiExtensionManager }

trait ExtensionManager extends ApiExtensionManager {
  def dumpExtensionPrimitives: String
  def dumpExtensions: String
  def reset(): Unit
}
