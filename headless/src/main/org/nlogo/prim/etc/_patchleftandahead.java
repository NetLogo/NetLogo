// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchleftandahead
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType(), Syntax.NumberType()},
            Syntax.PatchType(), "-T--");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context)
      throws LogoException {
    org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
    try {
      return turtle.getPatchAtHeadingAndDistance
          (-argEvalDoubleValue(context, 0),
              argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException exc) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
  }
}
