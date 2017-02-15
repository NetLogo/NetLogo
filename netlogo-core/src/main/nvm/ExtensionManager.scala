// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.TokenType
import org.nlogo.api.{ ExtensionManager => ApiExtensionManager }

trait ExtensionManager extends ApiExtensionManager {
  def dumpExtensionPrimitives: String
  def dumpExtensions: String
  def reset(): Unit

  /**
   * Returns cached type for a given primitive name.
   * Will be either TokenType.Command or a TokenType.Reporter
   *
   * This is cached so that it can be run without interrupting
   * the extension lifecycle hooks. It is cleared when clearAll is run.
   */
  def cachedType(name: String): Option[TokenType]
}
