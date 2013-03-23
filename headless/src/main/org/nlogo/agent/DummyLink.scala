// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// only used as a place holder by agent monitors

object DummyLink {
  private def toTurtle(x: AnyRef) =
    x match {
      case t: Turtle => t
      case _ => null
    }
}

class DummyLink(world: World, _end1: AnyRef, _end2: AnyRef, breed: AgentSet)
extends Link(world, DummyLink.toTurtle(_end1),
             DummyLink.toTurtle(_end2),
             world.getLinkVariablesArraySize(breed)) {
  variables(Link.VAR_BREED) = breed
  variables(Link.VAR_END1) = end1
  variables(Link.VAR_END2) = end2
  override def toString =
    world.getLinkBreedSingular(getBreed).toLowerCase + " " +
    Option(end1).map(_.id).getOrElse("?") + " " +
    Option(end2).map(_.id).getOrElse("?")
}
