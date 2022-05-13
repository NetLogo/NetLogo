// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.AgentKindJ;
import org.nlogo.core.Syntax;
import org.nlogo.api.LogoException;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.CustomAssembled;
import org.nlogo.nvm.SelfScoping;

public final class _createturtles
    extends Command
    implements CustomAssembled, SelfScoping {
  static final String NO_BREED = "";
  public final String breedName;

  public _createturtles() {
    breedName = NO_BREED;
    this.switches = true;
  }

  public _createturtles(String breedName) {
    this.breedName = breedName;
    this.switches = true;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      AgentSetBuilder agentSetBuilder = new AgentSetBuilder(AgentKindJ.Turtle(), numberOfTurtles);
      AgentSet breed =
          breedName == NO_BREED
              ? world.turtles()
              : world.getBreed(breedName);
      org.nlogo.api.MersenneTwisterFast random = context.job.random;
      for (int i = 0; i < numberOfTurtles; i++) {
        Turtle turtle =
            world.createTurtle(breed, random.nextInt(14),
                random.nextInt(360));
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
