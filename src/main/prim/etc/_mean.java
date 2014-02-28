// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _mean extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.ListType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) {
    double sum = 0;
    if (list.isEmpty()) {
      throw new EngineException(
        context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.emptyList"));
    }
    for (Iterator<Object> it = list.iterator(); it.hasNext();) {
      Object elt = it.next();
      if (!(elt instanceof Double)) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim._mean.cantFindMeanOfNonNumbers",
                Dump.logoObject(elt), TypeNames.name(elt)));
      }
      sum += ((Double) elt).doubleValue();
    }
    return validDouble(sum / list.size());
  }
}
