// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _shadeof
    extends Reporter
    implements org.nlogo.core.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    double color1 = argEvalDoubleValue(context, 0);
    double color2 = argEvalDoubleValue(context, 1);
    color1 = org.nlogo.api.Color.findCentralColorNumber(color1);
    color2 = org.nlogo.api.Color.findCentralColorNumber(color2);
    return color1 == color2 ? Boolean.TRUE : Boolean.FALSE;
  }


}
