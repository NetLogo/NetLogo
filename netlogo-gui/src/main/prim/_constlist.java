// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Dump;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _constlist
    extends Reporter implements Pure {
  final LogoList value;

  public _constlist(LogoList value) {
    this.value = value;
  }



  @Override
  public LogoList report(final org.nlogo.nvm.Context context) {
    return value;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + Dump.logoObject(value);
  }

  public LogoList report_1(final org.nlogo.nvm.Context context) {
    return value;
  }
}
