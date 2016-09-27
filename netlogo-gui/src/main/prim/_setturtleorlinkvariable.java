// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _setturtleorlinkvariable
    extends Command {
  String varName = "";

  public _setturtleorlinkvariable(_turtleorlinkvariable original) {
    varName = original.varName;
    this.switches = true;
  }



  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + varName;
    } else {
      return super.toString() + ":" + varName;
    }
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    Object value = args[0].report(context);
    try {
      context.agent.setTurtleOrLinkVariable(varName, value);
    } catch (AgentException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final Context context, Object value) throws LogoException {
    try {
      context.agent.setTurtleOrLinkVariable(varName, value);
    } catch (AgentException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    context.ip = next;
  }


}
