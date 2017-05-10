// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// this is a marker trait to identify instructions which correspond to variables
trait Variable

// this trait identifies variables which can be referenced (agent variables)
trait Referenceable {
  def makeReference: Reference
}
