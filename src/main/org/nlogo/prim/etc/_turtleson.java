package org.nlogo.prim.etc;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtleson
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_AGENT | Syntax.TYPE_AGENTSET},
            Syntax.TYPE_TURTLESET);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object agentOrSet = args[0].report(context);
    List<Turtle> resultList = new ArrayList<Turtle>();
    if (agentOrSet instanceof Turtle) {
      Turtle turtle = (Turtle) agentOrSet;
      if (turtle.id == -1) {
        throw new EngineException(context, this, I18N.errors().get("org.nlogo.$common.thatTurtleIsDead"));
      }
      addAll(resultList, turtle.getPatchHere().turtlesHere());
    } else if (agentOrSet instanceof Patch) {
      addAll(resultList, ((Patch) agentOrSet).turtlesHere());
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      if (sourceSet.type() == Turtle.class) {
        for (AgentSet.Iterator iter = sourceSet.iterator(); iter.hasNext();) {
          addAll(resultList, ((Turtle) iter.next()).getPatchHere().turtlesHere());
        }
      } else {
        for (AgentSet.Iterator iter = sourceSet.iterator(); iter.hasNext();) {
          addAll(resultList, ((Patch) iter.next()).turtlesHere());
        }
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.TYPE_AGENT | Syntax.TYPE_AGENTSET, agentOrSet);
    }
    return new org.nlogo.agent.ArrayAgentSet
        (Turtle.class,
            resultList.toArray
                (new Turtle[resultList.size()]),
            world);
  }

  private void addAll(List<Turtle> turtles, Iterable<Turtle> moreTurtles) {
    for (Turtle t : moreTurtles) {
      turtles.add(t);
    }
  }
}
