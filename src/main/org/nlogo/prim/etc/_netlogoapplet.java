package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _netlogoapplet
    extends Reporter {
  @Override
  public Object report(final Context context) {
    return workspace.getIsApplet()
        ? Boolean.TRUE
        : Boolean.FALSE;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.BooleanType());
  }
}
