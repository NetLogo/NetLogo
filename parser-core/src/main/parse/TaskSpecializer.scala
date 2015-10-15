// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.prim._task
import org.nlogo.core.{ReporterApp, AstTransformer}

// After we obtain an AST, we have a bunch of _commandtasks and _reportertasks which are
// the first argument to a _task. This replaces _task with the corresponding _commandtask/_reportertask
class TaskSpecializer extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case t: _task => super.visitReporterApp(app.args.head.asInstanceOf[ReporterApp])
      case _ => super.visitReporterApp(app)
    }
  }
}
