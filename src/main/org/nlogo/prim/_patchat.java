// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Patch;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchat
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(),
        Syntax.NumberType()};
    int ret = Syntax.PatchType();
    return Syntax.reporterSyntax(right, ret, "-TP-");
  }

  // I've tried to rejigger this and the result gets past TryCatchSafeChecker but then
  // doesn't work at runtime ("Inconsistent stack height") - ST 2/10/09
  @Override
  public Object report(final Context context) throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    Patch patch = null;
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy);
    } catch (org.nlogo.api.AgentException e) {
      return Nobody$.MODULE$;
    }
    return patch;
  }
}
