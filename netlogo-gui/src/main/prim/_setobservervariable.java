// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

public final strictfp class _setobservervariable
    extends Command {
  int vn = 0;

  public _setobservervariable(_observervariable original) {
    vn = original.vn;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()},
            true);
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.observerOwnsNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      context.agent.setObserverVariable(vn, args[0].report(context));
    } catch (org.nlogo.api.AgentException ex) {
      throw new org.nlogo.nvm.EngineException
          (context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final org.nlogo.nvm.Context context, Object arg0) throws LogoException {
    try {
      context.agent.setObserverVariable(vn, arg0);
    } catch (org.nlogo.api.AgentException ex) {
      throw new org.nlogo.nvm.EngineException
          (context, this, ex.getMessage());
    }

    context.ip = next;
  }
}
