// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.compile.api.{AstTransformer, Expression, ReporterApp, Statement}
import org.nlogo.prim._
import org.nlogo.prim.etc._member
import org.nlogo.prim._count

import scala.collection.mutable.Buffer

class AgentsetLazinessTransformer extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    val newApp = super.visitReporterApp(app)
    newApp.reporter match {
      case _: _other | _: _with =>
        val forceArgs =
          newApp.args.head match {
            case ReporterApp(_, _: _force, innerArgs, _) =>
              Buffer[Expression](newApp.copy(args = innerArgs))
            case _ => Buffer[Expression](newApp)
          }
        new ReporterApp(_force.coreprim(), new _force(), forceArgs, newApp.sourceLocation)

      case _: _count | _: _any | _: _member =>
        val forceArgs = newApp.args.head match {
          case ReporterApp(_, _: _force, innerArgs, _) =>
            innerArgs.toBuffer
          case _ => newApp.args.toBuffer
        }
        new ReporterApp(app.coreReporter, app.reporter, forceArgs, newApp.sourceLocation)

      case _ => newApp
    }
  }
}
