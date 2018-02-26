// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ ClosedVariable, Token }
import org.nlogo.nvm.{ AnonymousCommand, Context, LiftedLambda, Reporter, SelfScoping }

class _commandlambda(
  val argumentNames:   Seq[String],
  val argTokens:       Seq[Token],
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

  override def report(c: Context): AnyRef =
    AnonymousCommand(
      procedure = proc,
      formals   = proc.lambdaFormalsArray,
      binding   = c.activation.binding,
      locals    = c.activation.args,
      source    = lambdaSource)

}
