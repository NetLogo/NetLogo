package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _randompxcor extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_NUMBER);
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public double report_1(Context context) {
    int min = world.minPxcor();
    int max = world.maxPxcor();
    return min + context.job.random.nextInt(max - min + 1);
  }
}
