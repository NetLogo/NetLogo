package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _layoutcircle
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_TURTLESET | Syntax.TYPE_LIST,
            Syntax.TYPE_NUMBER},
            true);
  }

  @Override
  public void perform(final Context context) throws LogoException {
    Object nodes = args[0].report(context);
    double radius = argEvalDoubleValue(context, 1);
    try {
      if (nodes instanceof LogoList) {
        org.nlogo.agent.Layouts.circle
            (world, (LogoList) nodes, radius);
      } else {
        AgentSet set = (AgentSet) nodes;
        if (set.type() != Turtle.class) {
          throw new EngineException
              (context, this,
                  I18N.errors().get("org.nlogo.prim.etc._layoutcircle.patchesImmovable"));
        }
        org.nlogo.agent.Layouts.circle(set, radius, context.job.random);
      }
    } catch (org.nlogo.api.AgentException e) {
      throw new EngineException(context, this, e.getMessage());
    }
    context.ip = next;
  }
}
