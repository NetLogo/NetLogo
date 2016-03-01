// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _precision extends Reporter with Pure {
  override def report(context: Context): java.lang.Double =
    newValidDouble(
      org.nlogo.api.Approximate.approximate(
        argEvalDoubleValue(context, 0),
        argEvalIntValue(context, 1)))
}
