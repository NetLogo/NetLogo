package org.nlogo.prim.etc;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nlogo.api.Dump;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtleset
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_REPEATABLE | Syntax.TYPE_TURTLE
        | Syntax.TYPE_TURTLESET | Syntax.TYPE_NOBODY
        | Syntax.TYPE_LIST};
    int ret = Syntax.TYPE_TURTLESET;
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
        if (tempSet.type() != org.nlogo.agent.Turtle.class) {
          throw new ArgumentTypeException
              (context, this, i, Syntax.TYPE_TURTLE | Syntax.TYPE_TURTLESET, elt);
        }
        for (AgentSet.Iterator iter = tempSet.iterator();
             iter.hasNext();) {
          resultSet.add((Turtle) iter.next());
        }
      } else if (elt instanceof LogoList) {
        descendList(context, (LogoList) elt, resultSet);
      } else if (elt instanceof Turtle) {
        resultSet.add((Turtle) elt);
      } else if (!(elt instanceof org.nlogo.api.Nobody)) {
        throw new ArgumentTypeException
            (context, this, i, Syntax.TYPE_TURTLE | Syntax.TYPE_TURTLESET, elt);
      }
    }
    return new org.nlogo.agent.ArrayAgentSet(
        org.nlogo.agent.Turtle.class,
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
        if (tempSet.type() != org.nlogo.agent.Turtle.class) {
          throw new EngineException(context, this, I18N.errors().getNJava("org.nlogo.prim.etc._turtleset.listInputsMustBeTurtleOrTurtleAgentset",
              new String[]{this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)}));
        }
        for (AgentSet.Iterator iter2 = tempSet.iterator();
             iter2.hasNext();) {
          result.add((Turtle) iter2.next());
        }
      } else if (obj instanceof LogoList) {
        descendList(context, (LogoList) obj, result);
      } else if (!(obj instanceof org.nlogo.api.Nobody)) {
        throw new EngineException(context, this,
            I18N.errors().getNJava("org.nlogo.prim.etc._turtleset.incorrectInputType",
                new String[]{this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)}));
      }
    }
  }
}
