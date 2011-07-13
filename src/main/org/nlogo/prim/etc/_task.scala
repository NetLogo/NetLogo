package org.nlogo.prim.etc

import org.nlogo.nvm.{Context, Reporter, Syntax}

class _task extends Reporter {
  val lambda = Syntax.TYPE_COMMAND_TASK | Syntax.TYPE_REPORTER_TASK
  override def syntax = Syntax.reporterSyntax(
    Array(lambda), lambda)
  override def report(c: Context): AnyRef =
    args(0).report(c)
}
