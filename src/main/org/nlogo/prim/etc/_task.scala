// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _task extends Reporter {
  override def syntax = {
    val anyTask = Syntax.CommandTaskType | Syntax.ReporterTaskType
    Syntax.reporterSyntax(Array(anyTask), anyTask)
  }
  override def report(c: Context): AnyRef =
    args(0).report(c)
}
