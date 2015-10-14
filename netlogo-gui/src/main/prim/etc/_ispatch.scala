// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Patch
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _ispatch extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context) =
    Boolean.box(
      args(0).report(context).isInstanceOf[Patch])
}
