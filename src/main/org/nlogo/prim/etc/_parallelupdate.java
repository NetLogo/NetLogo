// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.api.Syntax;

public final strictfp class _parallelupdate
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.AgentsetType(), Syntax.ReferenceType(),
            Syntax.ReporterBlockType()},
            "OTPL", "?", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    AgentSet set = argEvalAgentSet(context, 0);

    Object[] results = new Object[set.count()];

    org.nlogo.nvm.Context freshContext =
        new org.nlogo.nvm.Context(context, set);
    int i = 0;
    args[1].checkAgentSetClass(set, context);
    for (AgentSet.Iterator it = set.iterator(); it.hasNext(); i++) {
      results[i] = freshContext.evaluateReporter(it.next(), args[1]);
    }

    try {
      i = 0;
      for (AgentSet.Iterator it = set.iterator(); it.hasNext(); i++) {
        it.next().setVariable(reference.vn(), results[i]);
      }
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException
          (context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
