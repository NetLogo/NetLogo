// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Observer;
import org.nlogo.core.Syntax;
import org.nlogo.core.Reference;
import org.nlogo.core.AgentKindJ;
import org.nlogo.nvm.Reporter;

public final strictfp class _observervariable
    extends Reporter {
  public int vn = 0;

  public _observervariable(int vn) {
    this.vn = vn;
  }

  public Reference makeReference() {
    return new Reference(AgentKindJ.Observer(), vn, this);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    return world.observer().getVariable(vn);
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.observerOwnsNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  public Object report_1(final org.nlogo.nvm.Context context) {
    return world.observer().getVariable(vn);
  }
}
