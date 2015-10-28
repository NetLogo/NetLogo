// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// it's very tempting to get rid of ride entirely but for the interface
// "riding turtle 0" I supposed we still need it. ev 4/29/05

// In the old days this was an integer instead of an enumeration, so in exported worlds it's still
// represented as an integer, hence the code here to convert back and forth to an integer at import
// or export time. - ST 3/18/08

// "class" not "trait" otherwise we won't get a static forwarder for Perspective.load() - ST 7/27/11

abstract sealed class Perspective(val export: Int) {
  def kind: Int = export
}

object Perspective {
  case object Observe extends Perspective(0)
  case class Ride(targetAgent: Agent) extends Perspective(1) with AgentFollowingPerspective {
    def followDistance: Int = 0
  }
  case class Follow(targetAgent: Agent, followDistance: Int) extends Perspective(2) with AgentFollowingPerspective
  case class Watch(targetAgent: Agent) extends Perspective(3)
}

trait AgentFollowingPerspective {
  def targetAgent: Agent
  def followDistance: Int
}
