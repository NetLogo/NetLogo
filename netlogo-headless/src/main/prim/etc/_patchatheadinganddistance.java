// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Nobody$;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchatheadinganddistance
    extends Reporter {

  @Override
  public Object report(final Context context) {
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
      return Nobody$.MODULE$;
    }
  }
}
