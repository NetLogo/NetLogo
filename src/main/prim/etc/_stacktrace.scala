// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ StackTraceBuilder, Context, Reporter }

class _stacktrace extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    StackTraceBuilder.build(
      context.activation, context.agent, this, None)
}
