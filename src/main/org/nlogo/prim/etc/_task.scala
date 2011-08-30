package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _task extends Reporter {
  val lambda = Syntax.CommandTaskType | Syntax.ReporterTaskType
  override def syntax =
    Syntax.reporterSyntax(Array(lambda), lambda)
  override def report(c: Context): AnyRef =
    args(0).report(c)
}
