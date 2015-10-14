// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final strictfp class _reverse
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[0].report(context);
    if (obj instanceof LogoList) {
      return ((LogoList) obj).reverse();
    } else if (obj instanceof String) {
      return new StringBuilder((String) obj).reverse().toString();
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.ListType() | Syntax.StringType()};
    int ret = Syntax.ListType() | Syntax.StringType();
    return Syntax.reporterSyntax(right, ret);
  }
}
