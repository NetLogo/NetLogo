// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{AgentSet}
import org.nlogo.core.{AgentKind}
import org.nlogo.nvm.{Context, Reporter}
import org.nlogo.core.Pure

class _islinkset extends Reporter with Pure {

  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.kind == AgentKind.Link
        case _ =>
          false
      })
}
