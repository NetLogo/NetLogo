// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _anywith
    extends Reporter {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0), args[1]);
  }

  public boolean report_1(Context context, AgentSet sourceSet, Reporter arg1)
      throws LogoException {
    Context freshContext = new Context(context, sourceSet);
    arg1.checkAgentSetClass(sourceSet, context);
    for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
      Agent tester = iter.next();
      Object value = freshContext.evaluateReporter(tester, arg1);
      if (!(value instanceof Boolean)) {
        throw new RuntimePrimitiveException
            (context, this, I18N.errorsJ().getN("org.nlogo.prim.$common.withExpectedBooleanValue",
                Dump.logoObject(tester), Dump.logoObject(value)));
      }
      if (((Boolean) value).booleanValue()) {
        return true;
      }
    }
    return false;
  }
}
