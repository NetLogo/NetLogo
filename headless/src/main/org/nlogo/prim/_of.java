// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _of
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.ReporterBlockType(),  // input on left
            new int[]{Syntax.AgentType() | Syntax.AgentsetType()}, // inputs on right
            Syntax.WildcardType(), // return type
            org.nlogo.api.Syntax.NormalPrecedence() + 1,
            true, // right associative
            "OTPL",
            "?"    // takes reporter block of unknown agent type
        );
  }

  @Override
  public Object report(final Context context) throws LogoException {
    Object agentOrSet = args[1].report(context);
    if (agentOrSet instanceof Agent) {
      Agent agent = (Agent) agentOrSet;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      args[0].checkAgentClass(agent, context);
      return new Context(context, agent).evaluateReporter(agent, args[0]);
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      LogoListBuilder result = new LogoListBuilder();
      Context freshContext = new Context(context, sourceSet);
      args[0].checkAgentSetClass(sourceSet, context);
      for (AgentIterator iter = sourceSet.shufflerator(context.job.random);
           iter.hasNext();) {
        result.add(freshContext.evaluateReporter(iter.next(), args[0]));
      }
      return result.toLogoList();
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 1,
              Syntax.AgentsetType() | Syntax.AgentType(),
              agentOrSet);
    }
  }
}
