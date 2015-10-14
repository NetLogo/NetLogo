// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Equality;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _position
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[1].report(context);
    if (obj instanceof LogoList) {
      Object value = args[0].report(context);
      LogoList list = (LogoList) obj;
      int i = 0;
      for (Iterator<Object> it = list.iterator(); it.hasNext();) {
        if (Equality.equals(value, it.next())) {
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

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType(),
        Syntax.ListType() | Syntax.StringType()};
    int ret = Syntax.NumberType() | Syntax.BooleanType();
    return Syntax.reporterSyntax(right, ret);
  }
}
