// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _extractrgb
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType()};
    int ret = Syntax.ListType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public LogoList report_1(final Context context, double color) {
    if (color < 0 || color >= 140)  //out of bounds
    {
      color = org.nlogo.api.Color.modulateDouble(color);  //modulate the color
    }
    return org.nlogo.api.Color.getRGBListByARGB
        (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(color));
  }
}
