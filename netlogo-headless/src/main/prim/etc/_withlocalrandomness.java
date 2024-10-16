// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.CustomAssembled;
import org.nlogo.nvm.SelfScoping;

public final class _withlocalrandomness
    extends Command
    implements CustomAssembled, SelfScoping {

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final Context context) {
    perform_1(context);
  }

  public void perform_1(final Context context) {
    AgentSet agentset = AgentSet.fromAgent(context.agent);
    org.nlogo.api.MersenneTwisterFast random = context.job.random;
    context.job.random = world.mainRNG().clone();
    context.runExclusiveJob(agentset, next);
    context.job.random = random;
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
