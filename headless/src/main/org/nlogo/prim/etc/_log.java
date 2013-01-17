// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _log extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType(), Syntax.NumberType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double n, double base) {
    if (n <= 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.cantTakeLogarithmOf", n));
    }
    if (base <= 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._log.notAValidBase", base));
    }
    return validDouble(StrictMath.log(n) / StrictMath.log(base));
  }
}
