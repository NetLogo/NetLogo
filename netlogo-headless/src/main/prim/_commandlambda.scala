// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.ClosedVariable
import org.nlogo.nvm.{ AnonymousCommand, Context, Procedure, Reporter }

class _commandlambda(val argumentNames: Seq[String], var proc: Procedure, val closedVariables: Set[ClosedVariable]) extends Reporter {

  override def toString =
    super.toString +
      // proc is null after ExpressionParser but before LambdaLifter
      Option(proc)
        .map(p => ":" + p.displayName)
        .getOrElse("")

  override def report(c: Context): AnyRef =
    AnonymousCommand(
      procedure = proc,
      formals = proc.lambdaFormals,
      binding = c.activation.binding.copy,
      locals = c.activation.args)

}
