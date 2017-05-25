// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _face
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
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"));
    }
    if (target.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", target.classDisplayName()));
    }
    if (context.agent instanceof Turtle) {
      ((Turtle) context.agent).face(target, true);
    } else {
      world.observer().face(target);
    }
    context.ip = next;
  }
}
