// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

// needed by _patchat.optimize() because regular _patchhere is turtle-only

public final strictfp class _patchhereinternal
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

  public Patch report_1(Context context) {
    if (context.agent instanceof Patch) {
      return (Patch) context.agent;
    } else if (context.agent instanceof Turtle) {
      return ((Turtle) context.agent).getPatchHere();
    } else {
      return world.fastGetPatchAt(0, 0);
    }
  }
}
