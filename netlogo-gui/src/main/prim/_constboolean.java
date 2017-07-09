// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _constboolean
    extends Reporter implements Pure {
  public final Boolean value;
  public final boolean primVal;

  public _constboolean(Boolean value) {
    this.value = value;
    primVal = value.booleanValue();
  }



  @Override
  public String toString() {
    return super.toString() + ":" + value;
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public Boolean report_1(Context context) {
    return value;
  }

  public boolean report_2(Context context) {
    return primVal;
  }
}
