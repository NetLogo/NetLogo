// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _breedhere
    extends Reporter {
  final String breedName;

  public _breedhere(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TurtlesetType(), "-TP-");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public AgentSet report_1(Context context) {
    Patch patch;
    if (context.agent instanceof Turtle) {
      patch = ((Turtle) context.agent).getPatchHere();
    } else {
      patch = (Patch) context.agent;
    }
    AgentSet agentset =
      new ArrayAgentSet(AgentKindJ.Turtle(), patch.turtleCount(),
            false, world);
    AgentSet breed = world.getBreed(breedName);
    for (Turtle turtle : patch.turtlesHere()) {
      if (turtle.getBreed() == breed) {
        agentset.add(turtle);
      }
    }
    return agentset;
  }
}
