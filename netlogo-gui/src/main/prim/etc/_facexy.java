// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final class _facexy
    extends Command {
  public _facexy() {
    this.switches = true;
  }



  @Override
  public void perform(final Context context)
      throws LogoException {
    if (context.agent instanceof org.nlogo.agent.Turtle) {
      Turtle turtle = (Turtle) context.agent;
      turtle.face(argEvalDoubleValue(context, 0),
          argEvalDoubleValue(context, 1),
          true);
    } else {
      world.observer().
          face(argEvalDoubleValue(context, 0),
              argEvalDoubleValue(context, 1));
    }
    context.ip = next;
  }
}
