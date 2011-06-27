package org.nlogo.prim.threed;

import org.nlogo.agent.Patch;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _patchat
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_PATCH;
    return Syntax.reporterSyntax(right, ret, "-TP-");
  }

  @Override
  public Object report(final Context context) throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    double dz = argEvalDoubleValue(context, 2);
    Patch patch = null;
    try {
      patch = ((org.nlogo.agent.Agent3D) (context.agent)).getPatchAtOffsets(dx, dy, dz);
    } catch (org.nlogo.api.AgentException e) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
    return patch;
  }
}
