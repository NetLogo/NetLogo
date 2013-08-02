// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter, Pure, CustomGenerated }

class _and extends Reporter with Pure with CustomGenerated {
  override def syntax =
    Syntax.reporterSyntax(
      Syntax.BooleanType,
      Array(Syntax.BooleanType),
      Syntax.BooleanType,
      Syntax.NormalPrecedence - 6)
  override def report(context: Context) =
    if (argEvalBooleanValue(context, 0))
      argEvalBoolean(context, 1)
    else
      java.lang.Boolean.FALSE
}
