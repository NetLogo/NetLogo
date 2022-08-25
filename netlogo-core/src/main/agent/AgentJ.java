// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

// This class exists to make commonly accessed values on agents fields instead of methods
// New values on agents should be added to "Agent" unless there is a compelling performance reason to put them here.
abstract class AgentJ {
  final World _world;
  Object[] _variables = null;
  public long _id = 0L;

  AgentJ(World world) {
    this._world = world;
  }
}
