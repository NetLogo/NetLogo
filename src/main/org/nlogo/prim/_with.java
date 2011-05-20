package org.nlogo.prim;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.I18NJava;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _with
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_AGENTSET,
            new int[]{Syntax.TYPE_BOOLEAN_BLOCK},
            Syntax.TYPE_AGENTSET,
            Syntax.NORMAL_PRECEDENCE + 2,
            false, // left associative
            "OTPL",
            "?"    // takes reporter block of unknown agent type
        );
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    return report_1
        (context, argEvalAgentSet(context, 0), args[1]);
  }

  public AgentSet report_1(final Context context, AgentSet sourceSet, Reporter reporterBlock)
      throws LogoException {
    Context freshContext = new Context(context, sourceSet);
    List<Agent> result = new ArrayList<Agent>();
    reporterBlock.checkAgentSetClass(sourceSet, context);
    for (AgentSet.Iterator iter = sourceSet.iterator(); iter.hasNext();) {
      Agent tester = iter.next();
      Object value = freshContext.evaluateReporter(tester, reporterBlock);
      if (!(value instanceof Boolean)) {
        throw new EngineException
            (context, this, I18NJava.errors().getN("org.nlogo.prim.$common.expectedBooleanValue",
                displayName(), Dump.logoObject(tester), Dump.logoObject(value)));
      }
      if (((Boolean) value).booleanValue()) {
        result.add(tester);
      }
    }
    return new org.nlogo.agent.ArrayAgentSet
        (sourceSet.type(),
            result.toArray(new Agent[result.size()]),
            world);
  }
}
