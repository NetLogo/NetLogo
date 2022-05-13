// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setturtlevariable
    extends Command {
  private final int vn;

  public _setturtlevariable(_turtlevariable original) {
    vn = original.vn;
    this.switches = true;
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
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final org.nlogo.nvm.Context context, Object value) throws LogoException {
    try {
      context.agent.setTurtleVariable(vn, value);
    } catch (org.nlogo.api.AgentException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
