// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

public final strictfp class _hatch
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  static final String NO_BREED = "";
  public final String breedName;

  public _hatch() {
    breedName = NO_BREED;
  }

  public _hatch(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(),
            Syntax.CommandBlockType() | Syntax.OptionalType()},
            "-T--", "-T--", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      Turtle parent = (Turtle) context.agent;
      AgentSet agentset =
        new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Turtle(), numberOfTurtles, false, world);
      if (breedName == NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.hatch();
          agentset.add(child);
          workspace.joinForeverButtons(child);
        }
      } else {
        AgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.hatch();
          child.setBreed(breed);
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
