// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

abstract class LinkFactory {
  def apply(world: World, src: Turtle, dest: Turtle, breed: AgentSet): Link
}
