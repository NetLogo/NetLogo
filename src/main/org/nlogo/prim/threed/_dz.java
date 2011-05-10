package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _dz
    extends Reporter {
  static final double INFINITESIMAL = 3.2e-15;

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_NUMBER, "-T--");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Turtle3D turtle = (org.nlogo.agent.Turtle3D) context.agent;
    double value = 0.0;
    value = turtle.dz();
    validDouble(value);
    if (StrictMath.abs(value) < INFINITESIMAL) {
      value = 0;
    }
    return Double.valueOf(value);
  }
}
