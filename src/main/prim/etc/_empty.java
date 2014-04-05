// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final strictfp class _empty
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    Object obj = args[0].report(context);
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      return list.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
    } else if (obj instanceof String) {
      String string = (String) obj;
      return (string.length() == 0) ? Boolean.TRUE : Boolean.FALSE;
    } else {
      throw new ArgumentTypeException(context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.ListType() | Syntax.StringType()};
    int ret = Syntax.BooleanType();
    return SyntaxJ.reporterSyntax(right, ret);
  }
}
