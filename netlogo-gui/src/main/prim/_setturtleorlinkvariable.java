// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.agent.Agent;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setturtleorlinkvariable
    extends Command {
  final String varName;
  final int vnTurtle;
  final int vnLink;

  public _setturtleorlinkvariable(String varName, int vnTurtle, int vnLink) {
    this.varName = varName;
    this.vnTurtle = vnTurtle;
    this.vnLink = vnLink;
    this.switches = true;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + varName;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    perform_1(context, args[0].report(context));
  }

  public void perform_1(final Context context, Object value) throws LogoException {
    Agent agent = context.agent;
    int agentBit = agent.agentBit();
    try {
      switch (agentBit) {
        case Turtle.BIT:
          agent.setTurtleVariable(vnTurtle, value);
          break;
        case Link.BIT:
          agent.setLinkVariable(vnLink, value);
          break;
        default:
          agent.setTurtleOrLinkVariable(varName, value);
          break;
      }
    } catch (AgentException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    context.ip = next;
  }
}
