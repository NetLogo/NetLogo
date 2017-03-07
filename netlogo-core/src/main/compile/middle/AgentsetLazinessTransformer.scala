// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.compile.api.{AstTransformer, Expression, ReporterApp, Statement}
import org.nlogo.core.{Token, TokenType}
import org.nlogo.nvm.AcceptsLazy
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
        val new_force = new _force()
        new_force.token = new Token("", TokenType.Reporter, "")(app.sourceLocation)
        new ReporterApp(_force.coreprim(), new_force, forceArgs, app.sourceLocation)

      case _: AcceptsLazy =>
        val forceArgs = newApp.args.map {
          case ReporterApp(_, _: _force, innerArg, _) => innerArg.head
          case arg => arg
        }
        new ReporterApp(app.coreReporter, app.reporter, forceArgs, app.sourceLocation)

      case _ => newApp
    }
  }
}
