// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _mean extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) throws LogoException {
    double sum = 0;

    if (list.isEmpty()) {
      String i18nMsg = I18N.errorsJ().getN(
        "org.nlogo.prim.etc._mean.cantFindMeanOfListWithNoNumbers", Dump.logoObject(list)
      );
      throw new RuntimePrimitiveException(context, this, i18nMsg);
    }

    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object elt = it.next();

      if (!(elt instanceof Double)) {
        String i18nMsg = I18N.errorsJ().getN(
          "org.nlogo.prim._mean.cantFindMeanOfNonNumbers", Dump.logoObject(elt), TypeNames.name(elt)
        );
        throw new RuntimePrimitiveException(context, this, i18nMsg);
      }

      sum += ((Double) elt).doubleValue();
    }

    return validDouble(sum / list.size(), context);
  }
}
