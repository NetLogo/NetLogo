// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _facexynowrap
    extends Command {
  public _facexynowrap() {
    this.switches = true;
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(), Syntax.NumberType()},
            "-T--");
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    Turtle turtle = (Turtle) context.agent;
    turtle.face(argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        false);
    context.ip = next;
  }
}
