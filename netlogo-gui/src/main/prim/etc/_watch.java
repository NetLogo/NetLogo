// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _watch
    extends Command {
  public _watch() {
    this.switches = true;
  }



  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    if (agent.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    world.observer().home();
    world.observer().setPerspective(PerspectiveJ.create(PerspectiveJ.WATCH, agent));
    context.ip = next;
  }
}
