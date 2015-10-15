// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.*;
import org.nlogo.api.Equality;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Reporter;

public final strictfp class _member
    extends Reporter
    implements Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    Object obj = args[1].report(context);
    if (obj instanceof LogoList) {
      Object value = args[0].report(context);
      LogoList list = (LogoList) obj;
      for (Object elt : list.toJava()) {
        if (Equality.equals(value, elt)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    } else if (obj instanceof String) {
      return ((String) obj).indexOf(argEvalString(context, 0)) != -1
          ? Boolean.TRUE
          : Boolean.FALSE;
    } else if (obj instanceof AgentSet) {
      Agent agent = argEvalAgent(context, 0);
      AgentSet agentset = (AgentSet) obj;
      if (agent instanceof Turtle) {
        if (agent.id() == -1) {
          return Boolean.FALSE;
        }
        if (agentset.kind() != AgentKindJ.Turtle()) {
          return Boolean.FALSE;
        }
        if (agentset == world.turtles()) {
          return Boolean.TRUE;
        }
        if (world.isBreed(agentset)) {
          return agentset == ((Turtle) agent).getBreed()
              ? Boolean.TRUE
              : Boolean.FALSE;
        }
      }
      if (agent instanceof Link) {
        if (agent.id() == -1) {
          return Boolean.FALSE;
        }
        if (agentset.kind() != AgentKindJ.Link()) {
          return Boolean.FALSE;
        }
        if (agentset == world.links()) {
          return Boolean.TRUE;
        }
        if (world.isBreed(agentset)) {
          return agentset == ((Link) agent).getBreed() ?
              Boolean.TRUE :
              Boolean.FALSE;
        }
      } else if (agent instanceof Patch) {
        if (agentset.kind() != AgentKindJ.Patch()) {
          return Boolean.FALSE;
        }
        if (agentset == world.patches()) {
          return Boolean.TRUE;
        }
      }
      return agentset.contains(agent)
          ? Boolean.TRUE
          : Boolean.FALSE;
    } else {
      throw new ArgumentTypeException
          (context, this, 1,
              Syntax.ListType() | Syntax.StringType() | Syntax.AgentsetType(),
              obj);
    }
  }

}
