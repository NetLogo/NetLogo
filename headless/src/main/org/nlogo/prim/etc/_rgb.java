// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _rgb
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()};
    int ret = Syntax.ListType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));
  }

  public LogoList report_1(final Context context, double r, double g, double b) {
    LogoListBuilder rgbList = new LogoListBuilder();
    rgbList.add(Double.valueOf(StrictMath.max(0, StrictMath.min(255, r))));
    rgbList.add(Double.valueOf(StrictMath.max(0, StrictMath.min(255, g))));
    rgbList.add(Double.valueOf(StrictMath.max(0, StrictMath.min(255, b))));
    return rgbList.toLogoList();
  }
}
