// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.ClosedVariable
import org.nlogo.nvm.{ AnonymousCommand, Context, LiftedLambda, Reporter }

import scala.collection.JavaConversions._

class _commandlambda(val argumentNames: Seq[String], var proc: LiftedLambda, val closedVariables: Set[ClosedVariable]) extends Reporter {
  override def report(c: Context): AnyRef = {
    AnonymousCommand(procedure = proc,
                formals   = proc.lambdaFormals,
                binding   = c.activation.binding.copy,
                locals    = c.activation.args)
  }
}
