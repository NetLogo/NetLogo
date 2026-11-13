// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Turtle;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _face
    extends Command {
  public _face() {
    this.switches = true;
  }



  @Override
  public void perform(final Context context)
      throws LogoException {
    perform_1(context, argEvalAgent(context, 0));
  }

  public void perform_1(final Context context, Agent target)
      throws LogoException {
    if (target instanceof org.nlogo.agent.Link) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Expected a turtle or a patch but got a link."));
    }
    if (target.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("That {} is dead.", target.classDisplayName()));
    }
    if (context.agent instanceof Turtle) {
      ((Turtle) context.agent).face(target, true);
    } else {
      world.observer().face(target);
    }
    context.ip = next;
  }
}
