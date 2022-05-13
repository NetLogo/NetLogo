// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _countother extends Reporter {


  @Override
  public Object report(Context context)
      throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0));
  }

  public double report_1(Context context, AgentSet arg0) {
    int result = 0;
    for (AgentIterator iter = arg0.iterator(); iter.hasNext();) {
      if (iter.next() != context.agent) {
        result++;
      }
    }
    return result;
  }
}
