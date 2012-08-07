// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.List;

public final strictfp class _minoneof
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.AgentsetType(), Syntax.NumberBlockType()};
    int ret = Syntax.AgentType();
    return Syntax.reporterSyntax(right, ret, "OTPL", "?");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    AgentSet sourceSet = argEvalAgentSet(context, 0);
    args[1].checkAgentSetClass(sourceSet, context);
    double winningValue = Double.MAX_VALUE;
    List<Agent> winners = new ArrayList<Agent>();
    org.nlogo.nvm.Context freshContext =
        new org.nlogo.nvm.Context(context, sourceSet);
    for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
      org.nlogo.agent.Agent tester = iter.next();
      Object result = freshContext.evaluateReporter(tester, args[1]);
      if (!(result instanceof Double)) {
        continue;
      }
      double dvalue = ((Double) result).doubleValue();
      // need to be careful here to handle properly the case where
      // dvalue equals Double.MAX_VALUE - ST 10/11/04
      if (dvalue <= winningValue) {
        if (dvalue < winningValue) {
          winningValue = dvalue;
          winners.clear();
        }
        winners.add(tester);
      }
    }
    if (winners.isEmpty()) {
      return org.nlogo.api.Nobody$.MODULE$;
    } else {
      return winners.get(context.job.random.nextInt(winners.size()));
    }
  }
}
