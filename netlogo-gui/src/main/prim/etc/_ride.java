// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _ride
    extends Command {
  public _ride() {
    this.switches = true;
  }



  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    Turtle turtle = argEvalTurtle(context, 0);
    if (turtle.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("That {} is dead.", turtle.classDisplayName()));
    }
    world.observer().setPerspective(PerspectiveJ.create(PerspectiveJ.RIDE, turtle));
    context.ip = next;
  }
}
