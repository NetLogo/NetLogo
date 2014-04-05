// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Reporter }

class _nopatches extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.PatchsetType)
  override def report(context: Context): AgentSet =
    world.noPatches
}
