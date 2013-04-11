// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// It would be nice to move this to the api package, but it would take a lot of refactoring to make
// all of the argument types and return types be the api types. - ST 4/11/13

trait LinkManager {
  def reset()
  def createLink(src: Turtle, dest: Turtle, breed: AgentSet): Link
  def findLink(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkFrom(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkEitherWay(src: Turtle, dest: Turtle, breed: AgentSet, includeAllLinks: Boolean): Link
  def findLinkedFrom(src: Turtle, sourceSet: AgentSet): AgentSet
  def findLinkedTo(target: Turtle, sourceSet: AgentSet): AgentSet
  def findLinkedWith(target: Turtle, sourceSet: AgentSet): AgentSet
  def checkBreededCompatibility(unbreeded: Boolean): Boolean
  def findLinksFrom(src: Turtle, breed: AgentSet): AgentSet
  def findLinksTo(target: Turtle, breed: AgentSet): AgentSet
  def findLinksWith(target: Turtle, breed: AgentSet): AgentSet
}

