// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.prim._
import org.nlogo.compile.api.{ AstTransformer, Expression, ReporterApp, Statement }

class AgentsetLazinessTransformer extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    val newApp = super.visitReporterApp(app)
    newApp.reporter match {
      case e: _other =>
        val forceArgs =
          newApp.args.head match {
            case ReporterApp(_, f: _force, innerArgs, loc) =>
              Seq(newApp.copy(args = innerArgs))
            case _ => Seq(newApp)
          }
        new ReporterApp(_force.coreprim(), new _force(), forceArgs, newApp.sourceLocation)
      case _ => newApp
    }
  }
}
