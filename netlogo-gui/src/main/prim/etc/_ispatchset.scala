// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{AgentSet, Patch}
import org.nlogo.core.{AgentKind, Syntax}
import org.nlogo.nvm.{Context, Reporter}
import org.nlogo.core.Pure

class _ispatchset extends Reporter with Pure {

  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.kind == AgentKind.Patch
        case _ =>
          false
      })
}
