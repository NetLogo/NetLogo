// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _breedsingular
    extends Reporter {
  private final String breedName;

  public _breedsingular(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType()},
            Syntax.TurtleType() | Syntax.NobodyType());
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public Object report_1(Context context, double idDouble)
      throws LogoException {
    long id = validLong(idDouble);
    if (id != idDouble) {
      throw new EngineException
          (context, this, idDouble + " is not an integer");
    }
    Turtle turtle = world.getTurtle(id);
    if (turtle == null) {
      return Nobody$.MODULE$;
    }
    AgentSet breed = world.getBreed(breedName);
    if (!breed.contains(turtle)) {
      throw new EngineException
          (context, this,
              turtle + " is not a " + world.getBreedSingular(breed));
    }
    return turtle;
  }
}
