// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _butlast
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.ListType() | Syntax.StringType()};
    int ret = Syntax.ListType() | Syntax.StringType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    return report_1(context, args[0].report(context));
  }

  public Object report_1(final org.nlogo.nvm.Context context, Object arg0)
      throws LogoException {
    if (arg0 instanceof LogoList) {
      LogoList list = (LogoList) arg0;
      if (list.isEmpty()) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.emptyListInput", displayName()));
      }
      return list.butLast();
    } else if (arg0 instanceof String) {
      String string = (String) arg0;
      if (string.length() == 0) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc.$common.emptyStringInput", token().name()));
      }
      return string.substring(0, string.length() - 1);
    } else {
      throw new ArgumentTypeException(context, this, 0, Syntax.ListType() |
          Syntax.StringType(), arg0);
    }
  }

  public String report_2(final org.nlogo.nvm.Context context, String arg0)
      throws LogoException {
    if (arg0.length() == 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.emptyStringInput", token().name()));
    }
    return arg0.substring(0, arg0.length() - 1);
  }

  public LogoList report_3(final org.nlogo.nvm.Context context, LogoList arg0)
      throws LogoException {
    if (arg0.isEmpty()) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.emptyListInput", displayName()));
    }
    return arg0.butLast();
  }
}
