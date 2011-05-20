package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.I18NJava;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _sqrt extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER};
    return Syntax.reporterSyntax(right, Syntax.TYPE_NUMBER);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double arg0) throws LogoException {
    if (arg0 < 0) {
      throw new EngineException(context, this,
          I18NJava.errors().getN("org.nlogo.prim.etc._sqrt.squareRootIsImaginary", arg0));
    }
    return StrictMath.sqrt(arg0);
  }
}
