// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.*;
import org.nlogo.core.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.CustomAssembled;
import org.nlogo.nvm.SelfScoping;

public final class _sprout
    extends Command
    implements CustomAssembled, SelfScoping {
  static final String NO_BREED = "";
  public final String breedName;

  public _sprout() {
    breedName = NO_BREED;
    this.switches = true;
  }

  public _sprout(String breedName) {
    this.breedName = breedName;
    this.switches = true;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    Patch parent = (Patch) context.agent;
    int numberOfTurtles = argEvalIntValue(context, 0);
    org.nlogo.api.MersenneTwisterFast random = context.job.random;
    if (numberOfTurtles > 0) {
      AgentSetBuilder agentSetBuilder = new AgentSetBuilder(AgentKindJ.Turtle(), numberOfTurtles);
      if (breedName == NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), world.turtles());
          agentSetBuilder.add(child);
          workspace.joinForeverButtons(child);
        }
      } else {
        AgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), breed);
          agentSetBuilder.add(child);
          workspace.joinForeverButtons(child);
        }
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
