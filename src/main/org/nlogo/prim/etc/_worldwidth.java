package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _worldwidth extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public Double report_1(Context context) {
    return world.worldWidthBoxed();
  }

  public double report_2(Context context) {
    return world.worldWidth();
  }
}
