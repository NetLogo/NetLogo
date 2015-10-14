// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _fastcreateturtles
    extends Command {
  private final String breedName;

  public _fastcreateturtles(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()},
            "O---", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    int numberOfTurtles = argEvalIntValue(context, 0);
    if (numberOfTurtles > 0) {
      AgentSet breed =
          breedName == _createturtles.NO_BREED
              ? world.turtles()
              : world.getBreed(breedName);
      org.nlogo.util.MersenneTwisterFast random = context.job.random;
      for (int i = 0; i < numberOfTurtles; i++) {
        Turtle turtle =
            world.createTurtle(breed, random.nextInt(14),
                random.nextInt(360));
        workspace.joinForeverButtons(turtle);
      }
    }
    context.ip = next;
  }
}
