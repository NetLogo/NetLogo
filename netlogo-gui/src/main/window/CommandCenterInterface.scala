// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

trait CommandCenterInterface extends Component {
  def repaintPrompt(): Unit
  def fitPrompt(): Unit
  def cycleAgentType(forward: Boolean): Unit
}
