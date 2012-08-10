// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Agent
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _isagent extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case agent: Agent =>
          agent.id != -1
        case _ =>
          false
      })
}
