// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

public final strictfp class _createturtles
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  static final String NO_BREED = "";
  public final String breedName;

  public _createturtles() {
    breedName = NO_BREED;
  }

  public _createturtles(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(),
            Syntax.CommandBlockType() | Syntax.OptionalType()},
            "O---", "-T--", true);
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
      AgentSet agentset =
        new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Turtle(), numberOfTurtles, false, world);
      AgentSet breed =
          breedName == NO_BREED
              ? world.turtles()
              : world.getBreed(breedName);
      org.nlogo.util.MersenneTwisterFast random = context.job.random;
      for (int i = 0; i < numberOfTurtles; i++) {
        Turtle turtle =
            world.createTurtle(breed, random.nextInt(14),
                random.nextInt(360));
        agentset.add(turtle);
        workspace.joinForeverButtons(turtle);
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
