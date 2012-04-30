// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ CommandTask, Context, Procedure, Reporter }

class _commandtask(proc: Procedure) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.CommandTaskType)

  override def toString =
    super.toString + ":" + proc.displayName

  override def report(c: Context): AnyRef =
    CommandTask(procedure = proc,
                formals = proc.taskFormals.reverse.dropWhile(_ == null).reverse.toArray,
                lets = c.allLets,
                locals = c.activation.args)

}
