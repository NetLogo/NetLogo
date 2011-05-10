package org.nlogo.prim.threed;

import org.nlogo.agent.World3D;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _randomzcor extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_NUMBER);
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public double report_1(Context context) {
    double min = ((World3D) world).minPzcor() - 0.5;
    double max = ((World3D) world).maxPzcor() + 0.5;
    return min + context.job.random.nextDouble() * (max - min);
  }
}
