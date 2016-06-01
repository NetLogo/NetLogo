// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ CommandTask, Context, Procedure, Reporter }

class _commandtask(val argCount: Int) extends Reporter {

  var proc: Procedure = null

  override def toString =
    super.toString + ":" + proc.displayName

  override def report(c: Context): AnyRef =
    CommandTask(procedure = proc,
                formals = proc.taskFormals.reverse.dropWhile(_ == null).reverse.toArray,
                lets = c.allLets,
                locals = c.activation.args)

}
