// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.core.Let
import org.nlogo.nvm.{ ReporterTask, Context, Reporter }

import scala.collection.JavaConversions._

class _reporterlambda(argumentNames: Seq[String]) extends Reporter {
  val formals: Seq[Let] = argumentNames.map(name => new Let(name))
  def formalsArray: Array[Let] = formals.toArray

  override def report(c: Context): AnyRef = {
    ReporterTask(body = args(0), formals = formalsArray, lets = c.allLets, locals = c.activation.args)
  }
}
