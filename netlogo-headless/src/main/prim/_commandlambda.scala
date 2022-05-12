// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.ClosedVariable
import org.nlogo.core.prim.Lambda
import org.nlogo.nvm.{ AnonymousCommand, Context, LambdaArgs, LiftedLambda, Reporter, SelfScoping }

class _commandlambda(
  val arguments:       Lambda.Arguments,
  var proc:            LiftedLambda,
  val closedVariables: Set[ClosedVariable],
  val lambdaSource:    String) extends Reporter with SelfScoping {

  source = lambdaSource

  override def toString =
    super.toString +
      // proc is null after ExpressionParser but before LambdaLifter
      Option(proc)
        .map(p => ":" + p.displayName)
        .getOrElse("")

  // These exist for backwards compatibility.  -Jeremy B December 2021
  def argumentNames = arguments.argumentNames
  def argTokens     = arguments.argumentTokens

  override def report(c: Context): AnyRef = {
    AnonymousCommand(
      procedure = proc,
      formals   = proc.lambdaFormalsArray,
      arguments = LambdaArgs.fromPrim(arguments),
      binding   = c.activation.binding,
      locals    = c.activation.args,
      source    = lambdaSource)
  }
}
