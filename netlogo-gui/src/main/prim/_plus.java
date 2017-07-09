// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _plus extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return validDouble
        (argEvalDoubleValue(context, 0) +
            argEvalDoubleValue(context, 1), context);
  }

  public double report_1(Context context, double d1, double d2) throws LogoException {
    return validDouble(d1 + d2, context);
  }
}
