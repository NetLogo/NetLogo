// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.core.AgentKindJ;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.SelfScoping;

public final class _createorderedturtles
    extends Command
    implements org.nlogo.nvm.CustomAssembled, SelfScoping {
  static final String NO_BREED = "";
  public final String breedName;

  public _createorderedturtles() {
    breedName = NO_BREED;
    this.switches = true;
  }

  public _createorderedturtles(String breedName) {
    this.breedName = breedName;
    this.switches = true;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      AgentSetBuilder agentSetBuilder = new AgentSetBuilder(AgentKindJ.Turtle(), numberOfTurtles);
      AgentSet breed =
          breedName == NO_BREED
              ? world.turtles()
              : world.getBreed(breedName);
      for (int i = 0; i < numberOfTurtles; i++) {
        Turtle turtle = world.createTurtle(breed);
        turtle.colorDouble(Double.valueOf(10.0 * i + 5.0));
        turtle.heading((360.0 * i) / numberOfTurtles);
        agentSetBuilder.add(turtle);
        workspace.joinForeverButtons(turtle);
      }
      context.runExclusiveJob(agentSetBuilder.build(), next);
    }
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
