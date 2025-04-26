// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.compile.api.{ DefaultAstVisitor, Optimizations, ReporterApp, Statement }

// "asInstanceOf" is everywhere here. Could I make it more type-safe? - ST 1/28/09

class Optimizer(optimizations: Optimizations) extends DefaultAstVisitor {

  override def visitStatement(stmt: Statement): Unit = {
    super.visitStatement(stmt)
    val oldCommand = stmt.command
    commandMungers.filter(_.clazz eq oldCommand.getClass)
      .find{munger => munger.munge(stmt); stmt.command != oldCommand}
  }

  override def visitReporterApp(app: ReporterApp): Unit = {
    super.visitReporterApp(app)
    val oldReporter = app.reporter
    reporterMungers.filter(_.clazz eq oldReporter.getClass)
      .find{munger => munger.munge(app); app.reporter != oldReporter}
  }

  private val commandMungers  = optimizations.commandOptimizations
  private val reporterMungers = optimizations.reporterOptimizations
 }
