// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.AgentException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _extractrgb
    extends Reporter {

  @Override
  public Object report(final org.nlogo.nvm.Context context)
    throws LogoException {
    Object val = args[0].report(context);

    if (val instanceof Double) {
      double d = (double) val;
      return report_1(context, d);
    }

    LogoList rgb = (LogoList) val;
    try {
      org.nlogo.api.Color.validRGBList(rgb,true);
    }
    catch (AgentException a) {
      throw new org.nlogo.nvm.RuntimePrimitiveException(
        context, this, I18N.errorsJ().getN("org.nlogo.prim.etc._extractrgb.invalidColor"));
    }
    LogoListBuilder rgbNoAlpha = new LogoListBuilder();
    for (int i = 0; i < 3; i++) {
      rgbNoAlpha.add(rgb.get(i));
    }
    return rgbNoAlpha.toLogoList();
  }

  public LogoList report_1(final Context context, double color) {
    if (color < 0 || color >= 140) {
      color = org.nlogo.api.Color.modulateDouble(color);  //modulate the color
    }
    return org.nlogo.api.Color.getRGBListByARGB
        (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(color));
  }
}
