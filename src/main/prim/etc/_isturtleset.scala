// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ AgentKind, Syntax, SyntaxJ }
import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _isturtleset extends Reporter with Pure {
  override def syntax =
    SyntaxJ.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.kind == AgentKind.Turtle
        case _ =>
          false
      })
}
