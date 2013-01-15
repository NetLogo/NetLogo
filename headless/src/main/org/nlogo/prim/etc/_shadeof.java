// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _shadeof
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    double color1 = argEvalDoubleValue(context, 0);
    double color2 = argEvalDoubleValue(context, 1);
    color1 = org.nlogo.api.Color.findCentralColorNumber(color1);
    color2 = org.nlogo.api.Color.findCentralColorNumber(color2);
    return color1 == color2 ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(),
        Syntax.NumberType()};
    int ret = Syntax.BooleanType();
    return Syntax.reporterSyntax(right, ret);
  }
}
