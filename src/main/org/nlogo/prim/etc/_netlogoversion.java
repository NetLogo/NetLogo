package org.nlogo.prim.etc;

import org.nlogo.api.Version;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Context;

public final strictfp class _netlogoversion extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_STRING);
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public String report_1(Context context) {
    return Version.versionNumberOnly();
  }
}
