// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _conststring extends Reporter implements Pure {
  final String value;

  public _conststring(String value) {
    this.value = value;
  }



  @Override
  public String toString() {
    return super.toString() + ":\"" + value + "\"";
  }

  @Override
  public Object report(Context context) {
    return value;
  }

  public String report_1(Context context) {
    return value;
  }
}
