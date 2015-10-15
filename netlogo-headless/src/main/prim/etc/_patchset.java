// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.api.Dump;
import org.nlogo.core.*;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.LinkedHashSet;
import java.util.Set;

public final strictfp class _patchset
    extends Reporter {

  @Override
  public Object report(final Context context) {
    LinkedHashSet<Patch> resultSet =
        new LinkedHashSet<Patch>();
    for (int i = 0; i < args.length; i++) {
      Object elt = args[i].report(context);
      if (elt instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) elt;
        if (tempSet.kind() != AgentKindJ.Patch()) {
          throw new ArgumentTypeException
              (context, this, i, Syntax.PatchType() | Syntax.PatchsetType(), elt);
        }
        for (AgentIterator iter = tempSet.iterator(); iter.hasNext();) {
          resultSet.add((Patch) iter.next());
        }
      } else if (elt instanceof LogoList) {
        descendList(context, (LogoList) elt, resultSet);
      } else if (elt instanceof Patch) {
        resultSet.add((Patch) elt);
      } else if (elt != Nobody$.MODULE$) {
        throw new ArgumentTypeException
            (context, this, i, Syntax.PatchType() | Syntax.PatchsetType(), elt);
      }
    }
    return AgentSet.fromArray(
      AgentKindJ.Patch(),
      resultSet.toArray(new org.nlogo.agent.Patch[resultSet.size()]));
  }

  private void descendList(Context context, LogoList tempList, Set<Patch> result) {
    for (Object obj : tempList.toJava()) {
      if (obj instanceof Patch) {
        result.add((Patch) obj);
      } else if (obj instanceof AgentSet) {
        AgentSet tempSet = (AgentSet) obj;
        if (tempSet.kind() != AgentKindJ.Patch()) {
          throw new EngineException(context, this,
              I18N.errorsJ().getN("org.nlogo.prim.etc._patchset.listInputNonPatchAgentset",
                  this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
        }
        for (AgentIterator iter2 = tempSet.iterator();
             iter2.hasNext();) {
          result.add((Patch) iter2.next());
        }
      } else if (obj instanceof LogoList) {
        descendList(context, (LogoList) obj, result);
      } else if (obj != Nobody$.MODULE$) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._patchset.listInputNonPatch",
                this.displayName(), Dump.logoObject(tempList, true, false), Dump.logoObject(obj, true, false)));
      }
    }
  }
}
