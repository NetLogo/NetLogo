// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed trait AgentKind {
  def mortal: Boolean
}

object AgentKind {
  case object Observer extends AgentKind { def mortal = false }
  case object Turtle   extends AgentKind { def mortal = true }
  case object Patch    extends AgentKind { def mortal = false }
  case object Link     extends AgentKind { def mortal = true }
}

/**
 * Java can't (I don't think) access Scala inner objects without reflection, so we provide these
 * convenience vals for use from Java.
 */
object AgentKindJ {
  val Observer = AgentKind.Observer
  val Turtle = AgentKind.Turtle
  val Patch = AgentKind.Patch
  val Link = AgentKind.Link
}
