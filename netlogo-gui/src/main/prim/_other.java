// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _other
    extends Reporter {
  // super( "TPL" ) ;


  @Override
  public Object report(final Context context)
      throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0));
  }

  public AgentSet report_1(final Context context, AgentSet sourceSet) {
    AgentSetBuilder result = new AgentSetBuilder(sourceSet.kind(), sourceSet.count());
    for (AgentIterator it = sourceSet.iterator(); it.hasNext();) {
      Agent otherAgent = it.next();
      if (context.agent != otherAgent) {
        result.add(otherAgent);
      }
    }
    return result.build();
  }
}
