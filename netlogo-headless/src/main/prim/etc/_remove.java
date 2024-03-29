// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Equality;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final class _remove
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    Object value = args[0].report(context);
    Object obj = args[1].report(context);
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      LogoListBuilder listCopy = new LogoListBuilder();
      for (Object elt : list.toJava()) {
        if (!Equality.equals(value, elt)) {
          listCopy.add(elt);
        }
      }
      return listCopy.toLogoList();
    } else if (obj instanceof String) {
      if (!(value instanceof String)) {
        throw new ArgumentTypeException(context, this, 0, Syntax.StringType(), value);
      }
      String string = (String) obj;
      String elt = (String) value;
      StringBuilder sbCopy = new StringBuilder();
      if (elt.length() <= string.length() && elt.length() != 0) {
        int i = 0;
        while (i <= string.length() - elt.length()) {
          if (!string.regionMatches(i, elt, 0, elt.length())) {
            sbCopy.append(string.charAt(i));
            i++;
          } else {
            i = i + elt.length();
          }
        }
        if (i > string.length() - elt.length() && (!string.regionMatches(i, elt, 0, elt.length()))) {
          sbCopy.append(string.substring(i));
        }
        return sbCopy.toString();
      } else {
        return string;
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

}
