// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter, Pure, CustomGenerated }

class _and extends Reporter with Pure with CustomGenerated {
  override def report(context: Context): java.lang.Boolean =
    if (argEvalBooleanValue(context, 0))
      argEvalBoolean(context, 1)
    else
      java.lang.Boolean.FALSE
}
