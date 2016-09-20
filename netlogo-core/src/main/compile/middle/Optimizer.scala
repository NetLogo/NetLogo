// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.Femto
import org.nlogo.nvm.{ Command, Instruction, Reporter }
import org.nlogo.compile.api.{ CommandMunger,
  DefaultAstVisitor, Optimizations, Match, ReporterApp,
  ReporterMunger, RewritingCommandMunger, RewritingReporterMunger, Statement }

// "asInstanceOf" is everywhere here. Could I make it more type-safe? - ST 1/28/09

class Optimizer(optimizations: Optimizations) extends DefaultAstVisitor {

  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    val oldCommand = stmt.command
    commandMungers.filter(_.clazz eq oldCommand.getClass)
      .find{munger => munger.munge(stmt); stmt.command != oldCommand}
  }

  override def visitReporterApp(app: ReporterApp) {
    super.visitReporterApp(app)
    val oldReporter = app.reporter
    reporterMungers.filter(_.clazz eq oldReporter.getClass)
      .find{munger => munger.munge(app); app.reporter != oldReporter}
  }

  private val commandMungers  = optimizations.commandOptimizations
  private val reporterMungers = optimizations.reporterOptimizations
 }
