// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter, StackTraceBuilder }

class _stacktrace extends Reporter {
  override def report(context: Context): String =
    StackTraceBuilder.build(
      context.activation, context.agent, this, None)
}
