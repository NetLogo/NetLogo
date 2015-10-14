// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _fastcreateorderedturtles
    extends Command {
  private final String breedName;

  public _fastcreateorderedturtles(String breedName) {
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
          breedName == _createorderedturtles.NO_BREED
              ? world.turtles()
              : world.getBreed(breedName);
      for (int i = 0; i < numberOfTurtles; i++) {
        Turtle turtle = world.createTurtle(breed);
        turtle.colorDouble(Double.valueOf(10.0 * i + 5.0));
        turtle.heading((360.0 * i) / numberOfTurtles);
        workspace.joinForeverButtons(turtle);
      }
    }
    context.ip = next;
  }
}
