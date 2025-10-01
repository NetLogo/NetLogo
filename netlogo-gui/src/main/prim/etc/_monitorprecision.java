// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _monitorprecision
    extends Reporter {


  @Override
  public Object report(final Context context) throws LogoException {
    Object value = "N/A";
    try {
      value = args[0].report(context);
    } catch (Exception e) {}
    int numberOfPlaces = argEvalIntValue(context, 1);
    if (!(value instanceof Double)) {
      return value;
    }
    String dStr = ((Double)value).toString();
    if (dStr.toLowerCase().contains("e")) {
      return String.format("%." + numberOfPlaces +  "E", (Double)value);
    }
    return newValidDouble
        (org.nlogo.api.Approximate.approximate
            (((Double) value).doubleValue(), numberOfPlaces), context);
  }
}
