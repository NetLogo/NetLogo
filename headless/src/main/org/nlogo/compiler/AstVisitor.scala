// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

/**
 * an interface for AST tree-walkers. This represents the usual Visitor
 * pattern with double-dispatch.
 */
trait AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition)
  def visitCommandBlock(block: CommandBlock)
  def visitReporterApp(app: ReporterApp)
  def visitReporterBlock(block: ReporterBlock)
  def visitStatement(stmt: Statement)
  def visitStatements(stmts: Statements)
}

/**
 * The default AST tree-walker. This simply visits each node of the
 * tree, and visits any children of each node in turn. Subclasses can
 * implement pre-order or post-order traversal, or a wide range of other
 * strategies.
 */
class DefaultAstVisitor extends AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition) { proc.statements.accept(this) }
  def visitCommandBlock(block: CommandBlock) { block.statements.accept(this) }
  def visitReporterApp(app: ReporterApp) { app.foreach(_.accept(this)) }
  def visitReporterBlock(block: ReporterBlock) { block.app.accept(this) }
  def visitStatement(stmt: Statement) { stmt.foreach(_.accept(this)) }
  def visitStatements(stmts: Statements) { stmts.foreach(_.accept(this)) }
}
