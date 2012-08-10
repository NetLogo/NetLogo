// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _length extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.ListType() | Syntax.StringType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    Object obj = args[0].report(context);
    if (obj instanceof LogoList) {
      return report_2(context, (LogoList) obj);
    } else if (obj instanceof String) {
      return report_3(context, (String) obj);
    }
    throw new ArgumentTypeException
        (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
  }

  public double report_1(Context context, Object obj) throws LogoException {
    if (obj instanceof LogoList) {
      return ((LogoList) obj).size();
    } else if (obj instanceof String) {
      return ((String) obj).length();
    }
    throw new ArgumentTypeException
        (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
  }

  public double report_2(Context context, LogoList list) {
    return list.size();
  }

  public double report_3(Context context, String s) {
    return s.length();
  }
}
