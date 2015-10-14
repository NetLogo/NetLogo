// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, Link }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _islinkset extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.`type` eq classOf[Link]
        case _ =>
          false
      })
}
