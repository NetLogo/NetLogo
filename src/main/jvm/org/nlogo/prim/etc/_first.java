// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _first
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.ListType() | Syntax.StringType()};
    int ret = Syntax.WildcardType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    return report_1(context, args[0].report(context));
  }

  public Object report_1(final org.nlogo.nvm.Context context, Object obj) {
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (list.isEmpty()) {
        throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyList"));
      }
      return list.first();
    } else if (obj instanceof String) {
      String string = (String) obj;
      if (string.length() == 0) {
        throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyString"));
      }
      return string.substring(0, 1);
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.ListType() | Syntax.StringType(), obj);
    }
  }

  public Object report_2(final org.nlogo.nvm.Context context, LogoList list) {
    if (list.isEmpty()) {
      throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyList"));
    }
    return list.first();
  }

  public Object report_3(final org.nlogo.nvm.Context context, String string) {
    if (string.length() == 0) {
      throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyString"));
    }
    return string.substring(0, 1);
  }
}
