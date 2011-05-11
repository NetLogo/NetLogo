package org.nlogo.prim.etc;

import java.util.Iterator;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _mean extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_LIST},
            Syntax.TYPE_NUMBER);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) throws LogoException {
    double sum = 0;
    for (Iterator<Object> it = list.iterator(); it.hasNext();) {
      Object elt = it.next();
      if (!(elt instanceof Double)) {
        throw new EngineException
            (context, this, I18N.errors().getNJava("org.nlogo.prim._mean.cantFindMeanOfNonNumbers",
                new String[]{Dump.logoObject(elt), Syntax.typeName(elt)}));
      }
      sum += ((Double) elt).doubleValue();
    }
    return validDouble(sum / list.size());
  }
}
