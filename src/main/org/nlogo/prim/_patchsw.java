// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchsw
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.PatchType(), "-TP-");
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public Object report_1(Context context) {
    Patch patch;
    if (context.agent instanceof Patch) {
      patch = ((Patch) context.agent).getPatchSouthWest();
    } else if (context.agent instanceof Turtle) {
      patch = ((Turtle) context.agent).getPatchHere().getPatchSouthWest();
    } else {
      patch = world.fastGetPatchAt(-1, -1);
    }
    if (patch == null) {
      return Nobody$.MODULE$;
    }
    return patch;
  }
}
