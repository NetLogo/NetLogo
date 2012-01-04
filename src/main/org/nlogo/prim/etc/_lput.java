// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _lput
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[0].report(context);
    return argEvalList(context, 1).lput(obj);
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType(),
        Syntax.ListType()};
    int ret = Syntax.ListType();
    return Syntax.reporterSyntax(right, ret);
  }
}
