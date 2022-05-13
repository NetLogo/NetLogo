// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Nobody$;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _turtle extends Reporter {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public Object report_1(Context context, double idDouble)
      throws LogoException {
    long id = validLong(idDouble, context);
    if (id != idDouble) {
      throw new RuntimePrimitiveException
          (context, this, idDouble + " is not an integer");
    }
    Turtle turtle = world.getTurtle(id);
    if (turtle == null) {
      return Nobody$.MODULE$;
    }
    return turtle;
  }
}
