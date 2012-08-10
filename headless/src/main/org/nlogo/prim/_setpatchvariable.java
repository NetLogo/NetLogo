// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _setpatchvariable
    extends Command {
  int vn = 0;

  public _setpatchvariable(_patchvariable original) {
    vn = original.vn;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()},
            "-TP-", true);
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    Object value = args[0].report(context);
    try {
      context.agent.setPatchVariable(vn, value);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final Context context, Object arg0)
      throws LogoException {
    try {
      context.agent.setPatchVariable(vn, arg0);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
