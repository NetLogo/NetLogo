// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final strictfp class _reverse
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
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

}
