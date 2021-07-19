// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.AgentException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _extractrgb
    extends Reporter {

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    try{
      double d = argEvalDoubleValue(context, 0);
      return report_1(context, d);
    }
    catch(LogoException l){
      org.nlogo.api.Exceptions.ignore( l ) ;
      LogoList rgb = argEvalList(context, 0);
      try{
        org.nlogo.api.Color.validRGBList(rgb,true);
      }
      catch(AgentException a){
        throw new org.nlogo.nvm.RuntimePrimitiveException(context, this, "Color must be a number or a valid RGB List.");
      }
      return rgb;
    }
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
