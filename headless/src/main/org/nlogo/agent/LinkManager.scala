// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// It would be nice to move this to the api package, but it would take a lot of refactoring to make
// all of the argument types and return types be the api types. - ST 4/11/13

trait LinkManager {

  def reset()

  def cleanupTurtle(turtle: Turtle)
  def cleanupLink(link: Link)

  def checkBreededCompatibility(unbreeded: Boolean): Boolean

  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link

  def findLink(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkEitherWay(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkFrom(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link

  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): Iterator[Turtle]
  def findLinkedTo(target: Turtle, sourceSet: AgentSet): Iterator[Turtle]
  def findLinkedWith(target: Turtle, sourceSet: AgentSet): Iterator[Turtle]

  def findLinksFrom(src: Turtle, breed: AgentSet): Iterator[Link]
  def findLinksTo(target: Turtle, breed: AgentSet): Iterator[Link]
  def findLinksWith(target: Turtle, breed: AgentSet): Iterator[Link]

  def tiedTurtles(root: Turtle): java.util.List[Turtle]

}
