package org.nlogo.prim.etc

import org.nlogo.nvm.{StackTraceBuilder, Context, Reporter, Syntax}

class _stacktrace extends Reporter {
  override def syntax = Syntax.reporterSyntax(Syntax.TYPE_STRING)
  override def report(context: Context) =
    StackTraceBuilder.getStackTrace(context.activation, context.agent, this, None)
}
