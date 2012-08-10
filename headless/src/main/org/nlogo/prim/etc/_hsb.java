// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _hsb
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

  public LogoList report_1(final Context context, double h, double s, double b) {
    int argb = java.awt.Color.HSBtoRGB
        ((float) (StrictMath.max
            (0, StrictMath.min(255, h)) / 255.0),
            (float) (StrictMath.max
                (0, StrictMath.min(255, s)) / 255.0),
            (float) (StrictMath.max
                (0, StrictMath.min(255, b)) / 255.0));

    LogoListBuilder rgbList = new LogoListBuilder();
    rgbList.add(Double.valueOf((argb >> 16) & 0xff));
    rgbList.add(Double.valueOf((argb >> 8) & 0xff));
    rgbList.add(Double.valueOf(argb & 0xff));
    return rgbList.toLogoList();
  }
}
