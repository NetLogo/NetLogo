// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait CommandCenterInterface {
  def repaintPrompt(): Unit
  def cycleAgentType(forward: Boolean): Unit
  def requestFocus(): Unit
}
