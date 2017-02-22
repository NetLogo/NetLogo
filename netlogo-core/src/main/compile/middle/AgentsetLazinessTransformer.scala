// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.compile.api.{AstTransformer, Expression, ReporterApp, Statement}
import org.nlogo.prim._
import org.nlogo.prim.etc._
import org.nlogo.prim._count

class AgentsetLazinessTransformer extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    val newApp = super.visitReporterApp(app)
    newApp.reporter match {
      case _: _other | _: _with =>
        val forceArgs =
          newApp.args.head match {
            case ReporterApp(_, _: _force, innerArg, _) =>
              Seq(newApp.copy(args = innerArg ++ newApp.args.tail))
            case _ => Seq(newApp)
          }
        new ReporterApp(_force.coreprim(), new _force(), forceArgs, newApp.sourceLocation)


      case _: _count | _: _any | _: _member | _: _nof | _: _oneof =>
//        val forceArgs = newApp.args.head match {
//          case ReporterApp(_, _: _force, innerArgs, _) =>
//            innerArgs
//          case _ => newApp.args
//        }
//        var forceArgs = Seq[Expression]()
//        for(a <- newApp.args) {
//          forceArgs = forceArgs :+
//          a match {
//            case ReporterApp(_, _: _force, innerArgs, _) => innerArgs
//            case _ => Seq(a)
//          }
//        }
        val forceArgs = newApp.args.map {
          case ReporterApp(_, _: _force, innerArg, _) => innerArg.head
          case arg => arg
        }
        new ReporterApp(app.coreReporter, app.reporter, forceArgs, newApp.sourceLocation)

      case _ => newApp
    }
  }
}
