// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, CommandBlock, ReporterApp, ReporterBlock, Statement, prim },
  prim._unknowncommand

import scala.util.matching.Regex

object NoopFolder extends PositionalAstFolder[Map[AstPath, Formatter.Operation]] {}

class RemovalVisitor(droppedCommand: String) extends PositionalAstFolder[Map[AstPath, Formatter.Operation]] {

  def delete(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context = ctx

  override def visitStatement(stmt: Statement, position: AstPath)(implicit ops: Map[AstPath, Formatter.Operation]): Map[AstPath, Formatter.Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(droppedCommand))
      super.visitStatement(stmt, position)(ops + (position -> delete _))
    else
      super.visitStatement(stmt, position)
  }
}

class ReplaceReporterVisitor(alteration: (String, String)) extends PositionalAstFolder[Map[AstPath, Formatter.Operation]] {
  import Formatter._

  def replace(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Context): Context = {
    astNode match {
      case app: ReporterApp =>
        ctx.copy(text = ctx.text + ctx.wsMap(path).leading + alteration._2)
      case _ => ctx
    }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Formatter.Operation] = {
    if (app.reporter.token.text.equalsIgnoreCase(alteration._1))
      super.visitReporterApp(app, position)(ops + (position -> replace _))
    else
      super.visitReporterApp(app, position)
  }
}

class AddVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case stmt: Statement =>
        val newCmd = new _unknowncommand(stmt.command.syntax)
        stmt.command.token.refine(newPrim = newCmd, text = addedCommand)
        val newArgs = addedArgument.map(id => Seq(stmt.args(id))).getOrElse(Seq())
        val newStmt = stmt.copy(command = newCmd, args = newArgs)
        val c1 =
          formatter.visitStatement(newStmt, position)(ctx.copy(
            text = ctx.text + " ",
            operations = ctx.operations - position,
            wsMap = newWsMap(ctx, position, stmt)))
        formatter.visitStatement(stmt, position)(c1.copy(
          text = c1.text + ctx.wsMap(position).trailing,
          operations = ctx.operations - position,
          wsMap = ctx.wsMap))
      case _               => ctx
    }
  }
}

// handles argument transfer for 1 argument at the moment. Should be expanded if more arguments are needed
class ReplaceVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case stmt: Statement =>
        stmt.command.token.refine(newPrim = stmt.command, text = addedCommand)
        val newStmt =
          if (addedArgument.isEmpty) stmt.copy(args = Seq())
          else stmt.copy(args = Seq(stmt.args(addedArgument.get)))
        val stmtAdded = formatter.visitStatement(newStmt, position)(
          ctx.copy(operations = ctx.operations - position, wsMap = newWsMap(ctx, position, stmt)))
        afterCommand
          .map(afterText => stmtAdded.copy(text = stmtAdded.text + afterText))
          .getOrElse(stmtAdded)
      case _ => ctx
    }
  }
}

// handles argument transfer for 1 argument at the moment. Should be expanded if more arguments are needed
trait StatementManipulationVisitor
  extends PositionalAstFolder[Map[AstPath, Formatter.Operation]] {
  import Formatter._

  def addition: (String, String)

  val pattern = new Regex("([^{]+)(\\{\\d+\\})?(.*)", "command", "arg", "afterCommand")

  val targetCommand = addition._1

  val addedCommand = pattern.findFirstMatchIn(addition._2)
    .map(_.group("command").stripSuffix(" ")).getOrElse(addition._2)
  val addedArgument = pattern.findFirstMatchIn(addition._2)
    .flatMap(m => Option(m.group("arg")))
    .map(_.drop(1).dropRight(1).toInt)
  val afterCommand = pattern.findFirstMatchIn(addition._2).map(_.group("afterCommand"))

  def newWsMap(ctx: Context, position: AstPath, stmt: Statement): Map[AstPath, WhiteSpace] =
    addedArgument.map { i =>
      val oldArgPosition = position / AstPath.Expression(stmt.args(i), i)
      val newArgPosition = position / AstPath.Expression(stmt.args(i), 0)
      ctx.wsMap.map {
        case (k, v) =>
          if (oldArgPosition.isParentOf(k))
            k.repath(oldArgPosition, newArgPosition) -> v
          else
            k -> v
      }
    }.getOrElse(ctx.wsMap)

 def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: Context): Context

  override def visitStatement(stmt: Statement, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(targetCommand))
      super.visitStatement(stmt, position)(ops + (position -> manipulate _))
    else
      super.visitStatement(stmt, position)
}
  }
