// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _layoutspring
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TurtlesetType(), Syntax.LinksetType(),
            Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()},
            true);
  }

  @Override
  public void perform(final Context context) {
    AgentSet nodeset = argEvalAgentSet(context, 0, AgentKindJ.Turtle());
    AgentSet linkset = argEvalAgentSet(context, 1, AgentKindJ.Link());
    double springConstant = argEvalDoubleValue(context, 2);
    double springLength = argEvalDoubleValue(context, 3);
    double repulsionConstant = argEvalDoubleValue(context, 4);
    org.nlogo.agent.Layouts.spring
      (world, nodeset, linkset, springConstant, springLength, repulsionConstant,
       context.job.random);
    context.ip = next;
  }
}
