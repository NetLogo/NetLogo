package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _monitorprecision
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.WildcardType(), Syntax.NumberType()},
            Syntax.NumberType(), "O---");
  }

  @Override
  public Object report(final Context context) throws LogoException {
    Object value = args[0].report(context);
    int numberOfPlaces = argEvalIntValue(context, 1);
    if (!(value instanceof Double)) {
      return value;
    }
    return newValidDouble
        (org.nlogo.api.Approximate.approximate
            (((Double) value).doubleValue(), numberOfPlaces));
  }
}
