// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

abstract class LinkFactory[A <: World] {
  def apply(world: A, src: Turtle, dest: Turtle, breed: AgentSet): Link
}
