// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _variance extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.ListType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) throws LogoException {
    double sum = 0, badElts = 0;
    int listSize = list.size();
    for (Iterator<Object> it = list.iterator(); it.hasNext();) {
      Object elt = it.next();
      if (!(elt instanceof Double)) {
        ++badElts;
        continue;
      }
      sum += ((Double) elt).doubleValue();
    }
    if (listSize - badElts < 2) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._variance.listMustHaveMoreThanOneNumber", Dump.logoObject(list)));
    }
    double mean = sum / (listSize - badElts);
    double squareOfDifference = 0;
    for (Iterator<Object> it = list.iterator(); it.hasNext();) {
      Object elt = it.next();
      if (elt instanceof Double) {
        squareOfDifference +=
            StrictMath.pow(((Number) elt).doubleValue() - mean, 2);
      }
    }
    return validDouble(squareOfDifference / (listSize - badElts - 1));
  }
}
