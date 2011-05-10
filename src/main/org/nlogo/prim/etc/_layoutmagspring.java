package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _layoutmagspring
    extends Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_TURTLESET, Syntax.TYPE_LINKSET,
        Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER, Syntax.TYPE_BOOLEAN};
    return Syntax.commandSyntax(right, true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    AgentSet nodeset = argEvalAgentSet(context, 0, Turtle.class);
    AgentSet linkset = argEvalAgentSet(context, 1, Link.class);
    double spr = argEvalDoubleValue(context, 2);
    double len = argEvalDoubleValue(context, 3);
    double rep = argEvalDoubleValue(context, 4);
    double magStr = argEvalDoubleValue(context, 5);
    int fieldType = argEvalIntValue(context, 6);
    boolean bidirectional = argEvalBooleanValue(context, 7);

    org.nlogo.agent.Layouts.magspring
        (nodeset, linkset, spr, len, rep, magStr, fieldType, bidirectional,
            context.job.random);
    context.ip = next;
  }
}
