// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait CommandCenterInterface {
  def cycleAgentType(forward: Boolean): Unit
  def repaintPrompt(): Unit
  def requestFocus(): Unit
  def outputArea: Option[OutputArea]
}
