package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Agent;
import org.nlogo.nvm.Context;
import static scala.collection.JavaConversions.asScalaBuffer;

public strictfp class _hubnetclearoverride
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]
            {Syntax.StringType(), Syntax.AgentsetType() | Syntax.AgentType(),
                Syntax.StringType()},
            "OTPL", "?", false);
  }

  @Override
  public void perform(final Context context) throws LogoException {
    final String client = argEvalString(context, 0);
    Object target = args[1].report(context);

    final String varName = argEvalString(context, 2);

    final AgentSet set;

    if (target instanceof Agent) {
      Agent agent = (Agent) target;
      set = new org.nlogo.agent.ArrayAgentSet(agent.getAgentClass(), 1, false, world);
      set.add(agent);
    } else {
      set = (AgentSet) target;
    }


    final java.util.List<Long> overrides = new java.util.ArrayList<Long>(set.count());

    for (AgentSet.Iterator iter = set.iterator(); iter.hasNext();) {
      Agent agent = iter.next();
      overrides.add(Long.valueOf(agent.id));
    }

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            workspace.getHubNetManager().clearOverride(client, set.type(), varName,
                asScalaBuffer(overrides));
          }
        });
    context.ip = next;
  }
}
