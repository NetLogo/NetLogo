// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _random extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(final Context context, double maxdouble) throws LogoException {
    long maxlong = validLong(maxdouble);
    if (maxdouble != maxlong) {
      maxlong += (maxdouble >= 0) ? 1 : -1;
    }
    if (maxlong > 0) {
      return context.job.random.nextLong(maxlong);
    }
    if (maxlong < 0) {
      return -context.job.random.nextLong(-maxlong);
    }
    return 0;
  }
}
