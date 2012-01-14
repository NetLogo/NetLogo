// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.api

sealed abstract class AgentType(val toInt: Int)

object AgentType {
  case object Turtle extends AgentType(0)
  case object Patch extends AgentType(1)
  case object Link extends AgentType(2)
  case object Observer extends AgentType(3)
  def fromAgentClass(agentClass: Class[_ <: api.Agent]): AgentType =
    if (agentClass == null)
      Observer
    else if (classOf[api.Turtle].isAssignableFrom(agentClass))
      Turtle
    else if (classOf[api.Patch].isAssignableFrom(agentClass))
      Patch
    else if (classOf[api.Link].isAssignableFrom(agentClass))
      Link
    else
      Observer
  def fromInt(i: Int): AgentType = i match {
    case 0 =>
      Turtle
    case 1 =>
      Patch
    case 2 =>
      Link
    case 3 =>
      Observer
  }
}

/**
 * Java can't (I don't think) access Scala inner objects without reflection, so we provide these
 * convenience vals for use from Java.
 */
object AgentTypeJ {
  import AgentType._
  val TURTLE = Turtle
  val PATCH = Patch
  val LINK = Link
  val OBSERVER = Observer
}
