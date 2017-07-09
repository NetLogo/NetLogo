// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.AgentKindJ;
import org.nlogo.core.Syntax;
import org.nlogo.api.LogoException;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.TreeAgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;

public final strictfp class _hatch
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  static final String NO_BREED = "";
  public final String breedName;

  public _hatch() {
    breedName = NO_BREED;
    this.switches = true;
  }

  public _hatch(String breedName) {
    this.breedName = breedName;
    this.switches = true;
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
      AgentSetBuilder agentSetBuilder = new AgentSetBuilder(AgentKindJ.Turtle(), numberOfTurtles);
      if (breedName == NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.hatch();
          agentSetBuilder.add(child);
          workspace.joinForeverButtons(child);
        }
      } else {
        TreeAgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.hatch();
          child.setBreed(breed);
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
