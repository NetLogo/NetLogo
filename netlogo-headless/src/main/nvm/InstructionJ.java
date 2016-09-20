// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

// Not sure if leaving this stuff in Java is strictly necessary.  It's
// possible that even if we converted to Scala, which would make these
// public fields into private fields hidden behind accessors, that
// HotSpot would cope and there'd be no performance impact. - ST 4/6/14

import org.nlogo.agent.World;
import org.nlogo.core.Reference;

public abstract class InstructionJ {
  public Workspace workspace;
  public World world;
  public Reporter[] args = new Reporter[0];
  public String agentClassString = "OTPL";
  public int agentBits = 0;
}
