// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ ClosedVariable, Let }
import org.nlogo.core.prim.Lambda
import org.nlogo.nvm.{ AnonymousReporter, Context, LambdaArgs, Reporter }

class _reporterlambda(
  val arguments:       Lambda.Arguments,
  val closedVariables: Set[ClosedVariable],
  lambdaSource:        String) extends Reporter {

  source = lambdaSource

  // This exists for backwards compatibility.  -Jeremy B December 2021
  def argumentNames = arguments.argumentNames

  val formals: Seq[Let] = arguments.argumentNames.map(name => Let(name))
  val formalsArray: Array[Let] = formals.toArray

  def getFormal(name: String): Option[Let] = formals.find(_.name == name)

  override def report(c: Context): AnyRef = {
    AnonymousReporter(
      body      = args(0),
      formals   = formalsArray,
      arguments = LambdaArgs.fromPrim(arguments),
      binding   = c.activation.binding,
      locals    = c.activation.args,
      source    = lambdaSource)
  }
}
