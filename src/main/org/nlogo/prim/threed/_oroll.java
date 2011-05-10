package org.nlogo.prim.threed;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _oroll
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_NUMBER, "O---");
  }

  @Override
  public Object report(final Context context) {
    return Double.valueOf(world.observer().roll());
  }
}
