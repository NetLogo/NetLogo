// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _sum extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList l0) throws LogoException {
    double sum = 0;
    for (Iterator<Object> it = l0.javaIterator(); it.hasNext();) {
      Object elt = it.next();
      if (elt instanceof Double) {
        sum += ((Double) elt).doubleValue();
      }
    }
    return validDouble(sum, context);
  }
}
