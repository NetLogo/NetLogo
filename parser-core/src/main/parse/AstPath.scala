// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, CommandBlock, Expression, ProcedureDefinition,
  ReporterApp, ReporterBlock, Statement, Statements }


object AstPath {
  trait Component
  case class Proc(name: String) extends Component
  case class Stmt(pos: Int) extends Component
  trait Exp extends Component {
    def pos: Int
  }
  case class RepArg(pos: Int) extends Exp
  case class CmdBlk(pos: Int) extends Exp
  case class RepBlk(pos: Int) extends Exp

  def Expression(exp: Expression, index: Int): Component = {
    exp match {
      case app: ReporterApp => RepArg(index)
      case cb: CommandBlock => CmdBlk(index)
      case rb: ReporterBlock => RepBlk(index)
    }
  }
}

import AstPath.Component

case class AstPath(components: Component*) {
  def /(c: Component) = AstPath((components :+ c): _*)

  // this is probably "too cute"
  def `../`: AstPath = AstPath(components.dropRight(1): _*)

  def last = components.last

  def isParentOf(other: AstPath): Boolean =
    other.components.length >= components.length &&
      other.components.take(components.length) == components

  def repath(fromRoot: AstPath, toRoot: AstPath): AstPath =
    AstPath((toRoot.components ++ components.drop(fromRoot.components.length)): _*)

  override def toString: String = {
    "AstPath(" + components.mkString(", ") + ")"
  }
}

trait PositionalAstFolder[A] {
  import AstPath._
  def visitProcedureDefinition(proc: ProcedureDefinition)(a: A): A = {
    visitStatements(proc.statements, AstPath(Proc(proc.procedure.name.toUpperCase)))(a)
  }
  def visitCommandBlock(block: CommandBlock, position: AstPath)(implicit a: A): A = {
    visitStatements(block.statements, position)
  }
  def visitExpression(exp: Expression, position: AstPath, index: Int)(implicit a: A): A = {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app, position / RepArg(index))
      case cb: CommandBlock =>
        visitCommandBlock(cb, position / CmdBlk(index))
      case rb: ReporterBlock =>
        visitReporterBlock(rb, position / RepBlk(index))
    }
  }

  def visitReporterApp(app: ReporterApp, position: AstPath)(implicit a: A): A = {
    app.args.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitExpression(arg, position, i)(acc)
    }
  }

  def visitReporterBlock(block: ReporterBlock, position: AstPath)(implicit a: A): A = {
    visitReporterApp(block.app, position)
  }

  def visitStatement(stmt: Statement, position: AstPath)(implicit a: A): A = {
    stmt.args.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitExpression(arg, position, i)(acc)
    }
  }

  def visitStatements(statements: Statements, position: AstPath)(implicit a: A): A = {
    statements.stmts.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitStatement(arg, position / Stmt(i))(acc)
    }
  }
}
