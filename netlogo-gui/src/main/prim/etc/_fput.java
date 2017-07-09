// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _fput
    extends Reporter
    implements org.nlogo.core.Pure {


  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    return report_1(context, args[0].report(context), argEvalList(context, 1));
  }

  public LogoList report_1(final org.nlogo.nvm.Context context, Object obj, LogoList list) {
    return list.fput(obj);
  }
}
