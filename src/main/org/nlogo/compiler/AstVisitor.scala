// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

/**
 * an interface for AST tree-walkers. This represents the usual Visitor
 * pattern with double-dispatch.
 */
trait AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition): Unit
  def visitCommandBlock(block: CommandBlock): Unit
  def visitReporterApp(app: ReporterApp): Unit
  def visitReporterBlock(block: ReporterBlock): Unit
  def visitStatement(stmt: Statement): Unit
  def visitStatements(stmts: Statements): Unit
}

/**
 * The default AST tree-walker. This simply visits each node of the
 * tree, and visits any children of each node in turn. Subclasses can
 * implement pre-order or post-order traversal, or a wide range of other
 * strategies.
 */
class DefaultAstVisitor extends AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition) = proc.statements.accept(this)
  def visitCommandBlock(block: CommandBlock) = block.statements.accept(this)
  def visitReporterApp(app: ReporterApp) = app.foreach(_.accept(this))
  def visitReporterBlock(block: ReporterBlock) = block.app.accept(this)
  def visitStatement(stmt: Statement) = stmt.foreach(_.accept(this))
  def visitStatements(stmts: Statements) = stmts.foreach(_.accept(this))
}
