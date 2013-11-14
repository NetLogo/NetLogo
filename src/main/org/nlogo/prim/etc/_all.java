// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _all
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.AgentsetType(), Syntax.BooleanBlockType()},
            Syntax.BooleanType(), "OTPL", "?");
  }

  @Override
  public Object report(final Context context) {
    return report_1
        (context, argEvalAgentSet(context, 0), args[1])
        ? Boolean.TRUE
        : Boolean.FALSE;
  }

  public boolean report_1(final Context context, AgentSet sourceSet, Reporter reporterBlock) {
    Context freshContext = new Context(context, sourceSet);
    reporterBlock.checkAgentSetClass(sourceSet, context);
    for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
      Agent tester = iter.next();
      Object value = freshContext.evaluateReporter(tester, reporterBlock);
      if (!(value instanceof Boolean)) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.$common.expectedBooleanValue",
                displayName(), Dump.logoObject(tester), Dump.logoObject(value)));
      }
      if (!((Boolean) value).booleanValue()) {
        return false;
      }
    }
    return true;
  }
}
