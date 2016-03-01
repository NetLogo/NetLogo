// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Dump;
import org.nlogo.core.*;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.LinkedHashSet;
import java.util.Set;

public final strictfp class _turtleset
    extends Reporter {

  @Override
  public Object report(final Context context) {
    LinkedHashSet<Turtle> resultSet =
        new LinkedHashSet<Turtle>();
    for (int i = 0; i < args.length; i++) {
      Object elt = args[i].report(context);
      if (elt instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) elt;
        if (tempSet.kind() != AgentKindJ.Turtle()) {
          throw new ArgumentTypeException
              (context, this, i, Syntax.TurtleType() | Syntax.TurtlesetType(), elt);
        }
        for (AgentIterator iter = tempSet.iterator();
             iter.hasNext();) {
          resultSet.add((Turtle) iter.next());
        }
      } else if (elt instanceof LogoList) {
        descendList(context, (LogoList) elt, resultSet);
      } else if (elt instanceof Turtle) {
        resultSet.add((Turtle) elt);
      } else if (elt != Nobody$.MODULE$) {
        throw new ArgumentTypeException
            (context, this, i, Syntax.TurtleType() | Syntax.TurtlesetType(), elt);
      }
    }
    return AgentSet.fromArray(
      AgentKindJ.Turtle(),
      resultSet.toArray(
        new org.nlogo.agent.Turtle[resultSet.size()]));
  }

  private void descendList(Context context, LogoList tempList, Set<Turtle> result) {
    for (Object obj : tempList.toJava()) {
      if (obj instanceof Turtle) {
        result.add((Turtle) obj);
      } else if (obj instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) obj;
        if (tempSet.kind() != AgentKindJ.Turtle()) {
          throw new EngineException(context, this,
              I18N.errorsJ().getN("org.nlogo.prim.etc._turtleset.listInputsMustBeTurtleOrTurtleAgentset",
                  this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
        }
        for (AgentIterator iter2 = tempSet.iterator();
             iter2.hasNext();) {
          result.add((Turtle) iter2.next());
        }
      } else if (obj instanceof LogoList) {
        descendList(context, (LogoList) obj, result);
      } else if (obj != Nobody$.MODULE$) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._turtleset.incorrectInputType",
                this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
      }
    }
  }
}
