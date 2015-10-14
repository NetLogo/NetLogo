// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;

public final strictfp class _setturtlevariable
    extends Command {
  private final int vn;

  public _setturtlevariable(_turtlevariable original) {
    vn = original.vn;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()}, "-T--", true);
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.turtlesOwnNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    Object value = args[0].report(context);
    try {
      context.agent.setTurtleVariable(vn, value);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final org.nlogo.nvm.Context context, Object value) throws LogoException {
    try {
      context.agent.setTurtleVariable(vn, value);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
