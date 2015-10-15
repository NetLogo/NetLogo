// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _ifelsevalue extends Reporter with Pure {
  override def report(context: Context): AnyRef =
    if (argEvalBooleanValue(context, 0))
      args(1).report(context)
    else
      args(2).report(context)
}
