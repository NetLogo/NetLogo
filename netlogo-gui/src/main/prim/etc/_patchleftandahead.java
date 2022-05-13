// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Reporter;

public final class _patchleftandahead
    extends Reporter {


  @Override
  public Object report(final org.nlogo.nvm.Context context)
      throws LogoException {
    org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
    try {
      return turtle.getPatchAtHeadingAndDistance
          (-argEvalDoubleValue(context, 0),
              argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException exc) {
      return org.nlogo.core.Nobody$.MODULE$;
    }
  }
}
