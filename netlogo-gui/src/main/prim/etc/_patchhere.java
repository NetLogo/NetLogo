// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchhere
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.PatchType(), "-T--");
  }

  @Override
  public Object report(final Context context) {
    return ((Turtle) context.agent).getPatchHere();
  }

  public Patch report_1(final Context context) {
    return ((Turtle) context.agent).getPatchHere();
  }
}
