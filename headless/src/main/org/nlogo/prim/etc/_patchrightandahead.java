// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchrightandahead
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType(), Syntax.NumberType()},
            Syntax.PatchType(), "-T--");
  }

  @Override
  public Object report(final Context context) {
    try {
      org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
      return turtle.getPatchAtHeadingAndDistance
          (argEvalDoubleValue(context, 0),
              argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException exc) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
  }
}
