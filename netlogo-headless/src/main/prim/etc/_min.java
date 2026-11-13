// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _min extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) {
    double winner = 0;
    Double boxedWinner = null;

    for (Object elt : list.toJava()) {
      if (elt instanceof Double) {
        Double boxedValue = (Double) elt;
        double value = boxedValue.doubleValue();
        if (boxedWinner == null || value < winner) {
          winner = value;
          boxedWinner = boxedValue;
        }
      }
    }
    if (boxedWinner == null) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Can't find the minimum of a list with no numbers: {}", Dump.logoObject(list)));
    }
    return boxedWinner;
  }
}
