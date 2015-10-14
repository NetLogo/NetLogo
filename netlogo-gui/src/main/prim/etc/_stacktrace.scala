// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ StackTraceBuilder, Context, Reporter }

class _stacktrace extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context) =
    StackTraceBuilder.build(
      context.activation, context.agent, this, None)
}
