// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _any
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.AgentsetType()},
            Syntax.BooleanType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0));
  }

  public boolean report_1(Context context, AgentSet arg0) {
    return !arg0.isEmpty();
  }
}
