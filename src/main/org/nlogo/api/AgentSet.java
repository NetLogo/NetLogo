package org.nlogo.api;

/**
 * Interface provides access to NetLogo agentsets.
 * NetLogo agentsets may be composed of turtles, patches, or links
 * (but an agentset may not contain a mix of different agent types.)
 * <p/>
 * In order to perform some functions on AgentSets
 * you may need to cast to org.nlogo.agent.AgentSet.
 */
public interface AgentSet {
  /**
   * Returns the name of the AgentSet, for all turtles, patches, or links returns its type,
   * for breeds returns the breed name for all other sets returns an empty string
   */
  String printName();

  /**
   * Returns the number of agents in the set
   */
  int count();

  /**
   * Returns the type of agents in the AgentSet
   */
  Class<? extends Agent> type();

  /**
   * Returns the world object that this AgentSet is associated with
   */
  World world();

  /**
   * Returns an iterable that cn be used to iterate through the agents in this set
   */
  Iterable<Agent> agents();

  /**
   * Reports true if this AgentSet and the given AgentSet have the exact same agents
   *
   * @param other the AgentSet to compare to
   */
  boolean equalAgentSets(AgentSet other);
}
