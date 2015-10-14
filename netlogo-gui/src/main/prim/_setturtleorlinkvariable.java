// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _setturtleorlinkvariable
    extends Command {
  String varName = "";

  public _setturtleorlinkvariable(_turtleorlinkvariable original) {
    varName = original.varName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()},
            "-T-L", true);
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
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  public void perform_1(final Context context, Object value) throws LogoException {
    try {
      context.agent.setTurtleOrLinkVariable(varName, value);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }


}
