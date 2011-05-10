package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _not
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_BOOLEAN},
            Syntax.TYPE_BOOLEAN);
  }

  @Override
  public Object report(Context context)
      throws LogoException {
    return report_1(context, argEvalBooleanValue(context, 0));
  }

  public boolean report_1(Context context, boolean arg0) {
    return !arg0;
  }
}
