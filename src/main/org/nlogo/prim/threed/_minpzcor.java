package org.nlogo.prim.threed;

import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _minpzcor
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    return Double.valueOf(((org.nlogo.agent.World3D) world).minPzcor());
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_NUMBER);
  }
}
