package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtle extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_NUMBER},
            Syntax.TYPE_TURTLE | Syntax.TYPE_NOBODY);
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
    return turtle;
  }
}
