// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _unaryminus extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return -argEvalDoubleValue(context, 0);
  }

  public double report_1(Context context, double d0) {
    return -d0;
  }
}
