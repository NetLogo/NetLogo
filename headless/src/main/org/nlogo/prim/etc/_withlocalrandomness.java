// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _withlocalrandomness
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.CommandBlockType()});
  }

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
    org.nlogo.util.MersenneTwisterFast random = context.job.random;
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
