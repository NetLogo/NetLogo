// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.ClosedVariable
import org.nlogo.nvm.{ AnonymousCommand, Context, LiftedLambda, Reporter }

import scala.collection.JavaConversions._

class _commandlambda(val argumentNames: Seq[String], val closedVariables: Set[ClosedVariable]) extends Reporter {
  var proc: LiftedLambda = null

  override def report(c: Context): AnyRef = {
    AnonymousCommand(procedure = proc,
                formals   = proc.lambdaFormals,
                lets      = c.allLets,
                locals    = c.activation.args)
  }
}
