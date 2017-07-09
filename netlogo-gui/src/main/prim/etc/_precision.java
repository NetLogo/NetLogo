// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _precision extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    double d = argEvalDoubleValue(context, 0);
    int numberOfPlaces = argEvalIntValue(context, 1);
    return validDouble(org.nlogo.api.Approximate.approximate
        (d, numberOfPlaces), context);
  }
}
