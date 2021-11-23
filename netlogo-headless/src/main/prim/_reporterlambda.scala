// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ ClosedVariable, Let }
import org.nlogo.nvm.{ AnonymousReporter, Context, Reporter }

class _reporterlambda(
  argumentNames:       Seq[String],
  val isVariadic:      Boolean,
  val closedVariables: Set[ClosedVariable],
  lambdaSource:        String) extends Reporter {

  val formals: Seq[Let] = argumentNames.map(name => new Let(name))
  def formalsArray: Array[Let] = formals.toArray

  def getFormal(name: String): Option[Let] = formals.find(_.name == name)

  override def report(c: Context): AnyRef = {
    AnonymousReporter(
      body       = args(0),
      formals    = formalsArray,
      isVariadic = isVariadic,
      binding    = c.activation.binding,
      locals     = c.activation.args,
      source     = lambdaSource)
  }
}
