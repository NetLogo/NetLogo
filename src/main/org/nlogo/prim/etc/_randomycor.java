package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _randomycor extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public double report_1(Context context) {
    double min = world.minPycor() - 0.5;
    double max = world.maxPycor() + 0.5;
    return min + context.job.random.nextDouble() * (max - min);
  }
}
