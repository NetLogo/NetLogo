// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Nobody, Perspective, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _subject extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.AgentType)
  override def report(context: Context) =
    if (world.observer.perspective == Perspective.Observe)
      Nobody
    else {
      val subject = world.observer.targetAgent
      // not actually sure if the null check here is necessary - ST 6/28/05
      if (subject == null || subject.id == -1)
        Nobody
      else
        subject
    }
}
