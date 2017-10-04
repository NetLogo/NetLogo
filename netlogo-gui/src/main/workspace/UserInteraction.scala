// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

// This trait is designed to abstract out the warningMessage function of the workspace so that it
// can be used by various workspace components. If it proves useful, it should be considered
// for consolidation with api.ControlSet. RG 10/6/17
trait UserInteraction {
  def warningMessage(text: String): Boolean
}

object DefaultUserInteraction extends UserInteraction {
  def warningMessage(text: String): Boolean = {
    System.err.println()
    System.err.println("WARNING: " + text)
    System.err.println()
    true
  }
}
