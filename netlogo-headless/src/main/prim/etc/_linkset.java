// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.api.Dump;
import org.nlogo.core.*;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

import java.util.LinkedHashSet;
import java.util.Set;

public final class _linkset
    extends Reporter {
  @Override
  public Object report(final Context context) {
    LinkedHashSet<Link> resultSet = new LinkedHashSet<Link>();
    for (int i = 0; i < args.length; i++) {
      Object elt = args[i].report(context);
      if (elt instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) elt;
        if (tempSet.kind() != AgentKindJ.Link()) {
          throw new ArgumentTypeException
              (context, this, i, Syntax.LinkType() | Syntax.LinksetType(), elt);
        }
        for (AgentIterator iter = tempSet.iterator(); iter.hasNext();) {
          resultSet.add((Link) iter.next());
        }
      } else if (elt instanceof LogoList) {
        descendList(context, (LogoList) elt, resultSet);
      } else if (elt instanceof Link) {
        resultSet.add((Link) elt);
      } else if (elt != Nobody$.MODULE$) {
        throw new ArgumentTypeException
            (context, this, i, Syntax.LinkType() | Syntax.LinksetType(), elt);
      }
    }
    return AgentSet.fromArray(
      AgentKindJ.Link(),
      resultSet.toArray(new org.nlogo.agent.Link[resultSet.size()]));
  }

  private void descendList(Context context, LogoList tempList, Set<Link> result) {
    for (Object obj : tempList.toJava()) {
      if (obj instanceof Link) {
        result.add((Link) obj);
      } else if (obj instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) obj;
        if (tempSet.kind() != AgentKindJ.Link()) {
          throw new RuntimePrimitiveException(context, this,
                I18N.errorsJ().getN("org.nlogo.prim.etc._linkset.invalidLAgentsetTypeInputToList",
                        this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
        }
        for (AgentIterator iter2 = tempSet.iterator();
             iter2.hasNext();) {
          result.add((Link) iter2.next());
        }
      } else if (obj instanceof LogoList) {
        descendList(context, (LogoList) obj, result);
      } else if (obj != Nobody$.MODULE$) {
        throw new RuntimePrimitiveException(context, this,
              I18N.errorsJ().getN("org.nlogo.prim.etc._linkset.invalidListInputs",
                      this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
      }
    }
  }
}
