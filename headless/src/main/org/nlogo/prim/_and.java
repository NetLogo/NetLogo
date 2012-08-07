// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _and
    extends Reporter
    implements org.nlogo.nvm.Pure, org.nlogo.nvm.CustomGenerated {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.BooleanType(),
        new int[]{Syntax.BooleanType()},
        Syntax.BooleanType(),
        org.nlogo.api.Syntax.NormalPrecedence() - 6);
  }

  @Override
  public Object report(final Context context) throws LogoException {
    return argEvalBooleanValue(context, 0)
        ? argEvalBoolean(context, 1)
        : Boolean.FALSE;
  }
}
