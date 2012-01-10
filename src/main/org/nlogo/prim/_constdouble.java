// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _constdouble
    extends Reporter implements Pure {
  final Double value;
  public final double primitiveValue;

  public _constdouble(Double value) {
    this.value = value;
    primitiveValue = value.doubleValue();
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.NumberType());
  }

  @Override
  public String toString() {
    return super.toString() + ":" + primitiveValue;
  }

  @Override
  public Object report(Context context) {
    return value;
  }

  public Double report_1(Context context) {
    return value;
  }

  public double report_2(Context context) {
    return primitiveValue;
  }
}
