// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _sprout
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  static final String NO_BREED = "";
  public final String breedName;

  public _sprout() {
    breedName = NO_BREED;
  }

  public _sprout(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(),
            Syntax.CommandBlockType() | Syntax.OptionalType()},
            "--P-", "-T--", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    Patch parent = (Patch) context.agent;
    int numberOfTurtles = argEvalIntValue(context, 0);
    org.nlogo.util.MersenneTwisterFast random = context.job.random;
    if (numberOfTurtles > 0) {
      AgentSet agentset =
        new ArrayAgentSet(AgentKindJ.Turtle(), numberOfTurtles,
              false, world);
      if (breedName == NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), world.turtles());
          agentset.add(child);
          workspace.joinForeverButtons(child);
        }
      } else {
        AgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.sprout
              (random.nextInt(14), random.nextInt(360), breed);
          agentset.add(child);
          workspace.joinForeverButtons(child);
        }
      }
      context.runExclusiveJob(agentset, next);
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
