package org.nlogo.prim.threed;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _layoutsphere
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_TURTLESET,
            Syntax.TYPE_NUMBER,
            Syntax.TYPE_NUMBER},
            true);
  }

  @Override
  public void perform(final Context context) throws LogoException {
    AgentSet set = argEvalAgentSet(context, 0);
    double radius = argEvalDoubleValue(context, 1);
    double initTemp = argEvalDoubleValue(context, 2);

    if (set.type() != Turtle.class) {
      throw new EngineException
          (context, this,
              "Patches are immovable.");
    }
    org.nlogo.agent.Layouts.sphere(set, radius, initTemp, context.job.random);
    context.ip = next;
  }
}
