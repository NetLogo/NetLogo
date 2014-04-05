// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _isundirectedlink extends Reporter with Pure {
  override def syntax =
    SyntaxJ.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context) match {
        case link: Link =>
          link.getBreed.isUndirected
        case _ =>
          false
      })
}
