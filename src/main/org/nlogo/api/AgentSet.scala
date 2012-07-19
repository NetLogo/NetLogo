// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface provides access to NetLogo agentsets.  NetLogo agentsets may be composed of turtles,
 * patches, or links (but an agentset may not contain a mix of different agent types.)
 *
 * In order to perform some functions on AgentSets you may need to cast to
 * org.nlogo.agent.AgentSet.
 */
trait AgentSet {

  /**
   * Returns the name of the AgentSet, for all turtles, patches, or links returns its type,
   * for breeds returns the breed name for all other sets returns an empty string
   */
  def printName: String

  /** Returns the number of agents in the set. */
  def count: Int

  /** Returns the type of agents in the AgentSet. */
  def `type`: Class[_ <: Agent]

  /** Returns the world object that this AgentSet is associated with. */
  def world: World

  /** Returns an iterable that cn be used to iterate through the agents in this set. */
  def agents: java.lang.Iterable[Agent]

  /**
   * Reports true if this AgentSet and the given AgentSet have the exact same agents
   *
   * @param other the AgentSet to compare to
   */
  def equalAgentSets(other: AgentSet): Boolean

  /**
   * Reports true if this is the breed agentset for a directed link breed
   *
   * @param other the AgentSet to compare to
   */
  def isDirected: Boolean

  /**
   * Reports true if this is the breed agentset for an undirected link breed
   *
   * @param other the AgentSet to compare to
   */
  def isUndirected: Boolean

}
