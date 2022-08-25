// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _patchatheadinganddistance
    extends Reporter {


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
      return org.nlogo.core.Nobody$.MODULE$;
    }
  }
}
