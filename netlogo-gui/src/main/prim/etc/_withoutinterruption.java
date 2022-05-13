// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.CustomAssembled;
import org.nlogo.nvm.SelfScoping;

public final class _withoutinterruption
    extends Command
    implements CustomAssembled, SelfScoping {


  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    perform_1(context);
  }

  public void perform_1(final Context context)
      throws LogoException {
    context.runExclusiveJob(AgentSet.fromAgent(context.agent), next);
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
