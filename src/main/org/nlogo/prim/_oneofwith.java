// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _oneofwith
    extends Reporter {

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.AgentsetType(), Syntax.BooleanBlockType()},
         Syntax.AgentType() | Syntax.NobodyType(), "OTPL", "?");
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0), args[1]);
  }

  public Object report_1(Context context, AgentSet sourceSet, Reporter arg1)
      throws LogoException {
    Context freshContext = new Context(context, sourceSet);
    arg1.checkAgentSetClass(sourceSet, context);
    for (AgentSet.Iterator iter = sourceSet.shufflerator(context.job.random); iter.hasNext();) {
      Agent tester = iter.next();
      Object value = freshContext.evaluateReporter(tester, arg1);
      if (!(value instanceof Boolean)) {
        throw new EngineException
            (context, this, I18N.errorsJ().getN("org.nlogo.prim.$common.withExpectedBooleanValue",
                Dump.logoObject(tester), Dump.logoObject(value)));
      }
      if (((Boolean) value).booleanValue()) {
        return tester;
      }
    }
    return Nobody$.MODULE$;
  }
}
