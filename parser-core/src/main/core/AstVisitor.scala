// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 * The default AST tree-walker. This simply visits each node of the
 * tree, and visits any children of each node in turn. Subclasses can
 * implement pre-order or post-order traversal, or a wide range of other
 * strategies.
 */
trait AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition): Unit = {
    visitStatements(proc.statements)
  }
  def visitCommandBlock(block: CommandBlock): Unit = {
    visitStatements(block.statements)
  }
  def visitExpression(exp: Expression): Unit = {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app)
      case cb: CommandBlock =>
        visitCommandBlock(cb)
      case rb: ReporterBlock =>
        visitReporterBlock(rb)
      case e =>
        throw new IllegalStateException
    }
  }
  def visitReporterApp(app: ReporterApp): Unit = {
    app.args.foreach(visitExpression)
  }
  def visitReporterBlock(block: ReporterBlock): Unit = {
    visitReporterApp(block.app)
  }
  def visitStatement(stmt: Statement): Unit = {
    stmt.args.foreach(visitExpression)
  }
  def visitStatements(stmts: Statements): Unit = {
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
      case e =>
        throw new IllegalStateException
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
    visitStatements(proc.statements)(using a)

  def visitCommandBlock(block: CommandBlock)(implicit a: A): A =
    visitStatements(block.statements)

  def visitExpression(exp: Expression)(implicit a: A): A =
    exp match {
      case app: ReporterApp  => visitReporterApp(app)
      case cb: CommandBlock  => visitCommandBlock(cb)
      case rb: ReporterBlock => visitReporterBlock(rb)
      case e => throw new IllegalStateException
    }

  def visitReporterApp(app: ReporterApp)(implicit a: A): A =
    app.args.foldLeft(a) { case (acc, arg) => visitExpression(arg)(using acc) }

  def visitReporterBlock(block: ReporterBlock)(implicit a: A): A =
    visitReporterApp(block.app)

  def visitStatement(stmt: Statement)(implicit a: A): A =
    stmt.args.foldLeft(a) { case (acc, arg) => visitExpression(arg)(using acc) }

  def visitStatements(statements: Statements)(implicit a: A): A =
    statements.stmts.foldLeft(a) {
      case (acc, arg) => visitStatement(arg)(using acc)
    }
}
