// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

// replaces _hatch when initialization block is empty

public final strictfp class _fasthatch
    extends Command {
  private final String breedName;

  public _fasthatch(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()},
            "-T--", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      Turtle parent = (Turtle) context.agent;
      if (breedName == _hatch.NO_BREED) {
        for (int i = 0; i < numberOfTurtles; i++) {
          workspace.joinForeverButtons(parent.hatch());
        }
      } else {
        AgentSet breed = world.getBreed(breedName);
        for (int i = 0; i < numberOfTurtles; i++) {
          Turtle child = parent.hatch();
          child.setBreed(breed);
          workspace.joinForeverButtons(child);
        }
      }
    }
    context.ip = next;
  }
}
