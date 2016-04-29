// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _isturtleset extends Reporter with Pure {

  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.`type` eq classOf[Turtle]
        case _ =>
          false
      })
}
