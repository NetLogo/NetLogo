// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Agent;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _subject
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    if (world.observer().perspective() == PerspectiveJ.OBSERVE()) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
    Agent subject = world.observer().targetAgent();
    // not actually sure if the null check here is necessary - ST 6/28/05
    if (subject == null || subject.id() == -1) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
    return subject;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.AgentType());
  }
}
