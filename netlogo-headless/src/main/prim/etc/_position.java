// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Equality;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final class _position
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    Object obj = args[1].report(context);
    if (obj instanceof LogoList) {
      Object value = args[0].report(context);
      LogoList list = (LogoList) obj;
      int i = 0;
      for (Object elt : list.toJava()) {
        if (Equality.equals(value, elt)) {
          return Double.valueOf(i);
        }
        i++;
      }
      return Boolean.FALSE;
    } else if (obj instanceof String) {
      String string = (String) obj;
      String elt = argEvalString(context, 0);
      int i = string.indexOf(elt);
      if (i == -1) {
        return Boolean.FALSE;
      } else {
        return Double.valueOf(i);
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

}
