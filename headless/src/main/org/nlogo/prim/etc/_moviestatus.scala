// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, ReporterRunnable }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _moviestatus extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context) =
    workspace.waitForResult(
      new ReporterRunnable[String] {
        override def run =
          workspace.movieStatus
      })
}
