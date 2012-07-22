// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

// replaces _sprout when initialization block is empty

public final strictfp class _fastsprout
    extends Command {
  private final String breedName;

  public _fastsprout(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()},
            "--P-", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Patch parent = (org.nlogo.agent.Patch) context.agent;
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      org.nlogo.agent.AgentSet agentset =
        new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Turtle(), numberOfTurtles,
              false, world);
      org.nlogo.util.MersenneTwisterFast random = context.job.random;

      if (breedName == _sprout.NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          org.nlogo.agent.Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), world.turtles());
          agentset.add(child);
        }
      } else {
        org.nlogo.agent.AgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          org.nlogo.agent.Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), breed);
          child.setBreed(breed);
          agentset.add(child);
        }
      }
    }
    context.ip = next;
  }
}
