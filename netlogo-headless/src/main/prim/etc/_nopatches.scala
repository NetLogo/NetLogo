// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _nopatches extends Reporter {
  override def report(context: Context): AgentSet =
    world.noPatches
}
