// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentIterator;
import org.nlogo.core.AgentKindJ;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.List;

public final class _anyturtleson
    extends Reporter {

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object agentOrSet = args[0].report(context);
    if (agentOrSet instanceof Turtle) {
      Turtle turtle = (Turtle) agentOrSet;
      if (turtle.id() == -1) {
        throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
      }
      return turtle.getPatchHere().turtlesHere().iterator().hasNext();
    } else if (agentOrSet instanceof Patch) {
      return ((Patch) agentOrSet).turtlesHere().iterator().hasNext();
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      if (sourceSet.kind() == AgentKindJ.Turtle()) {
        for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
          if (((Turtle) iter.next()).getPatchHere().turtlesHere().iterator().hasNext()) return true;
        }
        return false;
      } else {
        for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
          if (((Patch) iter.next()).turtlesHere().iterator().hasNext()) return true;
        }
        return false;
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.AgentType() | Syntax.AgentsetType(), agentOrSet);
    }
  }
}
