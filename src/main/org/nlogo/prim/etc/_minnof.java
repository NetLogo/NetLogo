// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

public final strictfp class _minnof
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.AgentsetType(), Syntax.NumberBlockType()};
    int ret = Syntax.AgentsetType();
    return Syntax.reporterSyntax(right, ret, "OTPL", "?");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    int n = argEvalIntValue(context, 0);
    if (n < 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.firstInputCantBeNegative", displayName()));
    }
    AgentSet sourceSet = argEvalAgentSet(context, 1);
    int count = sourceSet.count();
    if (n > count) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.notThatManyAgentsExist", n, count));
    }
    args[2].checkAgentSetClass(sourceSet, context);
    TreeMap<Object, LinkedList<Agent>> resultAgents =
        new TreeMap<Object, LinkedList<Agent>>();

    org.nlogo.nvm.Context freshContext =
        new org.nlogo.nvm.Context(context, sourceSet);
    for (AgentIterator iter = sourceSet.shufflerator(context.job.random);
         iter.hasNext();) {
      org.nlogo.agent.Agent tester = iter.next();
      Object result = freshContext.evaluateReporter(tester, args[2]);
      if (!(result instanceof Double)) {
        continue;
      }
      LinkedList<Agent> resultList = resultAgents.get(result);
      if (resultList == null) {
        resultList = new LinkedList<Agent>();
        resultAgents.put(result, resultList);
      }
      resultList.add(tester);
    }

    AgentSet resultSet = new org.nlogo.agent.ArrayAgentSet
        (sourceSet.kind(), n, false, world);

    for (Iterator<LinkedList<Agent>> iter = resultAgents.values().iterator();
         n > 0 && iter.hasNext();) {
      LinkedList<Agent> list = iter.next();
      for (Iterator<Agent> iter2 = list.iterator(); n > 0 && iter2.hasNext();) {
        resultSet.add(iter2.next());
        n--;
      }
    }

    return resultSet;
  }
}
