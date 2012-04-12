// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

// I'm pretty sure the compiler could optimize this out of existence and the only reason we don't do
// that is that we didn't get around to it.  This always wraps _commandtask or _reportertask and at
// runtime the extra layer of wrapping adds nothing, as far as I can see. - ST 4/11/12

class _task extends Reporter {
  override def syntax = {
    val anyTask = Syntax.CommandTaskType | Syntax.ReporterTaskType
    Syntax.reporterSyntax(Array(anyTask), anyTask)
  }
  override def report(c: Context): AnyRef =
    args(0).report(c)
}
