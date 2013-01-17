// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _extracthsb
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType() | Syntax.ListType()};
    int ret = Syntax.ListType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(Context context) {
    return report_1(context, args[0].report(context));
  }

  public LogoList report_1(Context context, Object obj) {
    if (obj instanceof LogoList) {
      LogoList list = (LogoList) obj;
      if (list.size() != 3) {
        throw new org.nlogo.nvm.EngineException
            (context, this, displayName() + " an rgb list must have 3 elements");
      }
      try {
        int argb = java.awt.Color.HSBtoRGB
            (StrictMath.max(0, StrictMath.min(255, ((Double) list.get(0)).intValue())) / 255,
                StrictMath.max(0, StrictMath.min(255, ((Double) list.get(1)).intValue())) / 255,
                StrictMath.max(0, StrictMath.min(255, ((Double) list.get(2)).intValue())) / 255);
        LogoListBuilder hsbList = new LogoListBuilder();
        hsbList.add(Double.valueOf((argb >> 16) & 0xff));
        hsbList.add(Double.valueOf((argb >> 8) & 0xff));
        hsbList.add(Double.valueOf(argb & 0xff));
        return hsbList.toLogoList();
      } catch (ClassCastException e) {
        throw new org.nlogo.nvm.EngineException
            (context, this, displayName() + " an rgb list must contain only numbers");
      }
    } else if (obj instanceof Double) {
      double color = ((Double) obj).doubleValue();
      if (color < 0 || color >= 140)   //out of bounds
      {
        // modulate the color
        color = org.nlogo.api.Color.modulateDouble(color);
      }
      return org.nlogo.api.Color.getHSBListByARGB
          (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber
              (color));
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 1, Syntax.ListType() | Syntax.NumberType(), obj);
    }
  }

  public LogoList report_2(Context context, double color) {
    if (color < 0 || color >= 140)  //out of bounds
    {
      // modulate the color
      color = org.nlogo.api.Color.modulateDouble(color);
    }
    return org.nlogo.api.Color.getHSBListByARGB
        (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber
            (color));
  }

  public LogoList report_3(Context context, LogoList list) {
    if (list.size() != 3) {
      throw new org.nlogo.nvm.EngineException
          (context, this, displayName() + " an rgb list must have 3 elements");
    }
    try {
      int argb = java.awt.Color.HSBtoRGB
          (StrictMath.max(0, StrictMath.min(255, ((Double) list.get(0)).intValue())) / 255,
              StrictMath.max(0, StrictMath.min(255, ((Double) list.get(1)).intValue())) / 255,
              StrictMath.max(0, StrictMath.min(255, ((Double) list.get(2)).intValue())) / 255);
      LogoListBuilder hsbList = new LogoListBuilder();
      hsbList.add(Double.valueOf((argb >> 16) & 0xff));
      hsbList.add(Double.valueOf((argb >> 8) & 0xff));
      hsbList.add(Double.valueOf(argb & 0xff));
      return hsbList.toLogoList();
    } catch (ClassCastException e) {
      throw new org.nlogo.nvm.EngineException
          (context, this, displayName() + " an rgb list must contain only numbers");
    }
  }
}
