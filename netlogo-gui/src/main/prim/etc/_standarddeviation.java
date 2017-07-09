// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _standarddeviation extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) throws LogoException {
    int listSize = list.size();
    double sum = 0, badElts = 0;
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object elt = it.next();
      if (elt instanceof Double) {
        sum += ((Double) elt).doubleValue();
      } else {
        ++badElts;
      }
    }
    if (listSize - badElts < 2) {
      throw new RuntimePrimitiveException(context, this, I18N.errorsJ().getN(
          "org.nlogo.prim.etc._standarddeviation.needListGreaterThanOneItem", Dump.logoObject(list)));
    }
    double mean = sum / (listSize - badElts);
    double squareOfDifference = 0;
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object elt = it.next();
      if (elt instanceof Double) {
        squareOfDifference +=
            StrictMath.pow(((Double) elt).doubleValue() - mean, 2);
      }
    }
    return validDouble
        (StrictMath.sqrt
            (squareOfDifference / (listSize - badElts - 1)), context);
  }
}
