// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.List;

public final strictfp class _withmax
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.AgentsetType(),
            new int[]{Syntax.NumberBlockType()},
            Syntax.AgentsetType(),
            org.nlogo.api.Syntax.NormalPrecedence() + 2,
            false, // left associative
            "OTPL",
            "?"    // takes reporter block of unknown agent type
        );
  }

  @Override
  public Object report(final Context context) {
    AgentSet sourceSet = argEvalAgentSet(context, 0);
    double winningValue = -Double.MAX_VALUE;
    Context freshContext = new Context(context, sourceSet);
    List<Agent> result = new ArrayList<Agent>();
    args[1].checkAgentSetClass(sourceSet, context);
    for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
      Agent tester = iter.next();
      Object value = freshContext.evaluateReporter(tester, args[1]);
      if (!(value instanceof Double)) {
        continue;
      }
      double dvalue = ((Double) value).doubleValue();
      if (dvalue >= winningValue) {
        if (dvalue > winningValue) {
          winningValue = dvalue;
          result.clear();
        }
        result.add(tester);
      }
    }
    return new org.nlogo.agent.ArrayAgentSet
        (sourceSet.kind(),
            result.toArray(new Agent[result.size()]),
            world);
  }
}
