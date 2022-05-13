// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _patchrightandahead
    extends Reporter {


  @Override
  public Object report(final Context context)
      throws LogoException {
    try {
      org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
      return turtle.getPatchAtHeadingAndDistance
          (argEvalDoubleValue(context, 0),
              argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException exc) {
      return org.nlogo.core.Nobody$.MODULE$;
    }
  }
}
