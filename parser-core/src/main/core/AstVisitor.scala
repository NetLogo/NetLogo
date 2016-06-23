// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 * The default AST tree-walker. This simply visits each node of the
 * tree, and visits any children of each node in turn. Subclasses can
 * implement pre-order or post-order traversal, or a wide range of other
 * strategies.
 */
trait AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition) {
    visitStatements(proc.statements)
  }
  def visitCommandBlock(block: CommandBlock) {
    visitStatements(block.statements)
  }
  def visitExpression(exp: Expression) {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app)
      case cb: CommandBlock =>
        visitCommandBlock(cb)
      case rb: ReporterBlock =>
        visitReporterBlock(rb)
    }
  }
  def visitReporterApp(app: ReporterApp) {
    app.args.foreach(visitExpression)
  }
  def visitReporterBlock(block: ReporterBlock) {
    visitReporterApp(block.app)
  }
  def visitStatement(stmt: Statement) {
    stmt.args.foreach(visitExpression)
  }
  def visitStatements(stmts: Statements) {
    stmts.stmts.foreach(visitStatement)
  }
}

/**
 * Transforms an AST to allow changes without mutation
 */
trait AstTransformer {
  def visitProcedureDefinition(proc: ProcedureDefinition): ProcedureDefinition = {
    proc.copy(statements = visitStatements(proc.statements))
  }
  def visitCommandBlock(block: CommandBlock): CommandBlock = {
    block.copy(statements = visitStatements(block.statements))
  }
  def visitExpression(exp: Expression): Expression = {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app)
      case cb: CommandBlock =>
        visitCommandBlock(cb)
      case rb: ReporterBlock =>
        visitReporterBlock(rb)
    }
  }
  def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.copy(args = app.args.map(visitExpression))
  }
  def visitReporterBlock(block: ReporterBlock): ReporterBlock = {
    block.copy(app = visitReporterApp(block.app))
  }
  def visitStatement(stmt: Statement): Statement = {
    stmt.copy(args = stmt.args.map(visitExpression))
  }
  def visitStatements(statements: Statements): Statements = {
    statements.copy(stmts = statements.stmts.map(visitStatement))
  }
}
