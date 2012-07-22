// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public final strictfp class _turtleset
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.RepeatableType() | Syntax.TurtleType()
        | Syntax.TurtlesetType() | Syntax.NobodyType()
        | Syntax.ListType()};
    int ret = Syntax.TurtlesetType();
    return Syntax.reporterSyntax(right, ret, 1, 0);
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
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
        for (AgentSet.Iterator iter = tempSet.iterator();
             iter.hasNext();) {
          resultSet.add((Turtle) iter.next());
        }
      } else if (elt instanceof LogoList) {
        descendList(context, (LogoList) elt, resultSet);
      } else if (elt instanceof Turtle) {
        resultSet.add((Turtle) elt);
      } else if (elt != org.nlogo.api.Nobody$.MODULE$) {
        throw new ArgumentTypeException
            (context, this, i, Syntax.TurtleType() | Syntax.TurtlesetType(), elt);
      }
    }
    return new org.nlogo.agent.ArrayAgentSet(
      AgentKindJ.Turtle(),
        resultSet.toArray(new org.nlogo.agent.Turtle[resultSet.size()]),
        world);
  }

  private void descendList(Context context, LogoList tempList, Set<Turtle> result)
      throws LogoException {
    for (Iterator<Object> iter = tempList.iterator();
         iter.hasNext();) {
      Object obj = iter.next();
      if (obj instanceof Turtle) {
        result.add((Turtle) obj);
      } else if (obj instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) obj;
        if (tempSet.kind() != AgentKindJ.Turtle()) {
          throw new EngineException(context, this,
              I18N.errorsJ().getN("org.nlogo.prim.etc._turtleset.listInputsMustBeTurtleOrTurtleAgentset",
                  this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
        }
        for (AgentSet.Iterator iter2 = tempSet.iterator();
             iter2.hasNext();) {
          result.add((Turtle) iter2.next());
        }
      } else if (obj instanceof LogoList) {
        descendList(context, (LogoList) obj, result);
      } else if (obj != org.nlogo.api.Nobody$.MODULE$) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._turtleset.incorrectInputType",
                this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
      }
    }
  }
}
