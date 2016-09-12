// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// these mirror the visitors in core, but are for the compiler AstNodes instead of core AstNodes

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
  def visitReporterApp(app: ReporterApp) { app.args.foreach(_.accept(this)) }
  def visitReporterBlock(block: ReporterBlock) { block.app.accept(this) }
  def visitStatement(stmt: Statement) { stmt.args.foreach(_.accept(this)) }
  def visitStatements(stmts: Statements) { stmts.stmts.foreach(_.accept(this)) }
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

trait AstFolder[A] {
  def visitProcedureDefinition(proc: ProcedureDefinition)(a: A): A =
    visitStatements(proc.statements)(a)

  def visitCommandBlock(block: CommandBlock)(implicit a: A): A =
    visitStatements(block.statements)

  def visitExpression(exp: Expression)(implicit a: A): A =
    exp match {
      case app: ReporterApp  => visitReporterApp(app)
      case cb: CommandBlock  => visitCommandBlock(cb)
      case rb: ReporterBlock => visitReporterBlock(rb)
    }

  def visitReporterApp(app: ReporterApp)(implicit a: A): A =
    app.args.foldLeft(a) { case (acc, arg) => visitExpression(arg)(acc) }

  def visitReporterBlock(block: ReporterBlock)(implicit a: A): A =
    visitReporterApp(block.app)

  def visitStatement(stmt: Statement)(implicit a: A): A =
    stmt.args.foldLeft(a) { case (acc, arg) => visitExpression(arg)(acc) }

  def visitStatements(statements: Statements)(implicit a: A): A =
    statements.stmts.foldLeft(a) {
      case (acc, arg) => visitStatement(arg)(acc)
    }
}
