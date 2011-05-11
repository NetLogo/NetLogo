package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _self
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_AGENT, "-TPL");
  }

  @Override
  public Object report(final Context context) {
    return context.agent;
  }

  public Agent report_1(final Context context) {
    return context.agent;
  }
}
