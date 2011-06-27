package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _patchatheadinganddistance
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER},
            Syntax.TYPE_PATCH, "-TP-");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    try {
      double heading = argEvalDoubleValue(context, 0);
      if (heading < 0 || heading >= 360) {
        heading = ((heading % 360) + 360) % 360;
      }
      return world.protractor().getPatchAtHeadingAndDistance
          (context.agent,
              heading,
              argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException exc) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
  }
}
