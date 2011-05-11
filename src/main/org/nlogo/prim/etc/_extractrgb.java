package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _extractrgb
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_LIST;
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {

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
