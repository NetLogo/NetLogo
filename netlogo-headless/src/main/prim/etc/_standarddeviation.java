// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _standarddeviation extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) {
    int listSize = list.size();
    double sum = 0, badElts = 0;
    for (Object elt : list.toJava()) {
      if (elt instanceof Double) {
        sum += ((Double) elt).doubleValue();
      } else {
        ++badElts;
      }
    }
    if (listSize - badElts < 2) {
      throw new EngineException(context, this, I18N.errorsJ().getN(
          "org.nlogo.prim.etc._standarddeviation.needListGreaterThanOneItem", Dump.logoObject(list)));
    }
    double mean = sum / (listSize - badElts);
    double squareOfDifference = 0;
    for (Object elt : list.toJava()) {
      if (elt instanceof Double) {
        squareOfDifference +=
            StrictMath.pow(((Double) elt).doubleValue() - mean, 2);
      }
    }
    return validDouble
        (StrictMath.sqrt
            (squareOfDifference / (listSize - badElts - 1)));
  }
}
