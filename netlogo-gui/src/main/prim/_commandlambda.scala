// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ ClosedVariable, Token }
import org.nlogo.nvm.{ AnonymousCommand, Context, LiftedLambda, Reporter }

import scala.collection.JavaConversions._

class _commandlambda(
  val argumentNames:   Seq[String],
  val argTokens:       Seq[Token],
  var proc:            LiftedLambda,
  val closedVariables: Set[ClosedVariable],
  val lambdaSource:    String) extends Reporter {

  source = lambdaSource

  override def report(c: Context): AnyRef = {
    AnonymousCommand(
      procedure = proc,
      formals   = proc.lambdaFormalsArray,
      binding   = c.activation.binding.copy,
      locals    = c.activation.args,
      source    = lambdaSource)
  }
}
