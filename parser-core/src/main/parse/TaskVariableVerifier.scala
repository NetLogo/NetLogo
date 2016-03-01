// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{prim, AstTransformer, ReporterApp, Fail, I18N},
  prim.{_taskvariable, _commandtask, _reportertask},
  Fail._

import scala.collection.mutable

// This verifies that all _taskvariables are contained within a _commandtask or _reportertask.
// Additionally, it adds a count of the _taskvariables to that _commandtask or _reportertask.
// This count is later used by MiddleEnd to generate the correctly sized array of formals.
class TaskVariableVerifier extends AstTransformer {
  val argsCount = mutable.Stack[Int]()

  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case ct: _commandtask =>
        val (newApp, count) = countTaskArgs(app)
        newApp.copy(reporter = ct.copy(minArgCount = count))
      case rt: _reportertask =>
        val (newApp, count) = countTaskArgs(app)
        newApp.copy(reporter = rt.copy(minArgCount = count))
      case tv: _taskvariable => cAssert(
        argsCount.nonEmpty, I18N.errors.get("compiler.TaskVisitor.notDefined"), app)
        argsCount.update(0, math.max(argsCount.head, tv.vn))
        super.visitReporterApp(app)
      case _ => super.visitReporterApp(app)
    }
  }

  private def countTaskArgs(app: ReporterApp): (ReporterApp, Int) = {
    argsCount.push(0)
    val newApp = super.visitReporterApp(app)
    val count = argsCount.pop()
    (newApp, count)
  }
}
