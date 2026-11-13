// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _item
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    int index = argEvalIntValue(context, 0);
    Object obj = args[1].report(context);
    if (index < 0) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("{} isn't greater than or equal to zero.", Integer.toString(index)));
    }
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (index >= list.size()) {
        throw new RuntimePrimitiveException(context, this,
          LocaleContextJ.loc("Can't find element {} of the list {}, which is only of length {}.",
                             Integer.toString(index), Dump.logoObject(list), Integer.toString(list.size())));
      }
      return list.get(index);
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (index >= string.length()) {
        throw new RuntimePrimitiveException(context, this,
          LocaleContextJ.loc("Can't find element {} of the list {}, which is only of length {}.",
                             Integer.toString(index), Dump.logoObject(string), Integer.toString(string.length())));
      }
      return string.substring(index, index + 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

}
