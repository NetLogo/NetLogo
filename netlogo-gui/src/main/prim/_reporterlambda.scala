// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.core.{ ClosedVariable, Let }
import org.nlogo.nvm.{ AnonymousReporter, Context, Reporter }

import scala.collection.JavaConversions._

class _reporterlambda(argumentNames: Seq[String], val closedVariables: Set[ClosedVariable]) extends Reporter {
  val formals: Seq[Let] = argumentNames.map(name => new Let(name))
  def formalsArray: Array[Let] = formals.toArray

  override def report(c: Context): AnyRef = {
    AnonymousReporter(body = args(0), formals = formalsArray, binding = c.activation.binding, locals = c.activation.args)
  }
}
