// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _tostring extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, args[0].report(context));
  }

  public String report_1(Context context, Object arg0) {
    return arg0.toString();
  }
}
