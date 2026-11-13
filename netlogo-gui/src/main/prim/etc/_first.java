// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _first
    extends Reporter
    implements org.nlogo.core.Pure {


  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    return report_1(context, args[0].report(context));
  }

  public Object report_1(final org.nlogo.nvm.Context context, Object obj)
      throws LogoException {
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (list.isEmpty()) {
        throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("List is empty."));
      }
      return list.first();
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (string.length() == 0) {
        throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("String is empty."));
      }
      return string.substring(0, 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

  public Object report_2(final org.nlogo.nvm.Context context, LogoList list)
      throws LogoException {
    if (list.isEmpty()) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("List is empty."));
    }
    return list.first();
  }

  public Object report_3(final org.nlogo.nvm.Context context, String string)
      throws LogoException {
    if (string.length() == 0) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("String is empty."));
    }
    return string.substring(0, 1);
  }
}




