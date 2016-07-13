// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.core.Let
import org.nlogo.nvm.{ CommandTask, Context, Procedure, Reporter }

import scala.collection.JavaConversions._

class _commandlambda(val argumentNames: Seq[String]) extends Reporter {
  var proc: Procedure = null

  override def report(c: Context): AnyRef =
    CommandTask(procedure = proc,
                formals   = proc.taskFormals,
                lets      = c.allLets,
                locals    = c.activation.args)
}
