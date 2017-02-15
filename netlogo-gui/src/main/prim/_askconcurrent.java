// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _askconcurrent
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  public _askconcurrent() {
    this.switches = true;
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    AgentSet agentset = argEvalAgentSet(context, 0);
    if (!(context.agent instanceof org.nlogo.agent.Observer)) {
      if (agentset == world.turtles()) {
        throw new RuntimePrimitiveException
            (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"));
      }
      if (agentset == world.patches()) {
        throw new RuntimePrimitiveException
            (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllPatches"));
      }
    }
    if (context.makeChildrenExclusive()) {
      context.runExclusiveJob(agentset, next);
    } else {
      context.waiting = true;
      workspace.addJobFromJobThread(context.makeConcurrentJob(agentset));
    }
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
