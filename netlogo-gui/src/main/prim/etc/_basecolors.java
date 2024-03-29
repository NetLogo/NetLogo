// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _basecolors
    extends Reporter {
  private static final LogoList colors = cache();

  private static LogoList cache() {
    LogoListBuilder result = new LogoListBuilder();
    for (int i = 0; i < 14; i++) {
      result.add(Double.valueOf(i * 10 + 5));
    }
    return result.toLogoList();
  }



  @Override
  public Object report(Context context) {
    return colors;
  }

  public LogoList report_1(Context context) {
    return colors;
  }
}
