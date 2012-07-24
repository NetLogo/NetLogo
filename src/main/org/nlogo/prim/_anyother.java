// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _anyother extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.AgentsetType()},
            Syntax.BooleanType(),
            "-TPL");
  }

  @Override
  public Object report(Context context)
      throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0));
  }

  public boolean report_1(Context context, AgentSet sourceSet) {
    for (AgentIterator it = sourceSet.iterator(); it.hasNext();) {
      Agent otherAgent = it.next();
      if (context.agent != otherAgent) {
        return true;
      }
    }
    return false;
  }
}
