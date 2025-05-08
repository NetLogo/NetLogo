// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, ReporterApp, Statement, prim },
  prim._unknowncommand

import scala.util.matching.Regex

import org.nlogo.core.{ Reporter, SourceLocation, Syntax, Token, TokenType }

class _dummyrep(text: String) extends Reporter {
  val syntax = Syntax.reporterSyntax(ret = Syntax.WildcardType)
  token = Token(text, TokenType.Reporter, text)(SourceLocation(0, 0, ""))
}

class _dummycmd(text: String) extends Reporter {
  val syntax = Syntax.commandSyntax()
  token = Token(text, TokenType.Command, text)(SourceLocation(0, 0, ""))
}

object NoopFolder extends PositionalAstFolder[AstEdit] {}

class RemovalVisitor(droppedCommand: String) extends PositionalAstFolder[AstEdit] {

  def delete(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: AstFormat): AstFormat = ctx

  override def visitStatement(stmt: Statement, position: AstPath)(implicit edits: AstEdit): AstEdit = {
    if (stmt.command.token.text.equalsIgnoreCase(droppedCommand))
      super.visitStatement(stmt, position)(using edits.addOperation(position, delete))
    else
      super.visitStatement(stmt, position)
  }
}

class ReplaceReporterVisitor(alteration: (String, String)) extends PositionalAstFolder[AstEdit] {
  def replace(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: AstFormat): AstFormat = {
    astNode match {
      case app: ReporterApp =>
        ctx.copy(text = ctx.text + ctx.wsMap.leading(path) + alteration._2)
      case _ => ctx
    }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit edits: AstEdit): AstEdit = {
    if (app.reporter.token.text.equalsIgnoreCase(alteration._1))
      super.visitReporterApp(app, position)(using edits.addOperation(position, replace))
    else
      super.visitReporterApp(app, position)
  }
}

class AddVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: AstFormat): AstFormat = {
    astNode match {
      case stmt: Statement =>
        val newCmd = new _unknowncommand(stmt.command.syntax)
        stmt.command.token.refine(newPrim = newCmd, text = addedCommand)
        val newArgs = addedArgument.map(id => Seq(stmt.args(id))).getOrElse(Seq())
        val newStmt = stmt.copy(command = newCmd, args = newArgs)
        val c1 =
          formatter.visitStatement(newStmt, position / AstPath.Stmt(-1))(using ctx.copy(
            text = ctx.text + " ",
            operations = ctx.operations - position))
        formatter.visitStatement(stmt, position)(using c1.copy(
          text = c1.text + ctx.wsMap.trailing(position),
          operations = ctx.operations - position))
      case _               => ctx
    }
  }
}

// handles argument transfer for 1 argument at the moment. Should be expanded if more arguments are needed
class ReplaceVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: AstFormat): AstFormat = {
    astNode match {
      case stmt: Statement =>
        stmt.command.token.refine(newPrim = stmt.command, text = addedCommand)
        val newStmt =
          if (addedArgument.isEmpty) stmt.copy(args = Seq())
          else stmt.copy(args = Seq(stmt.args(addedArgument.get)))
        val stmtAdded = formatter.visitStatement(newStmt, position / AstPath.Stmt(-1))(using
          ctx.copy(operations = ctx.operations - position))
        afterCommand
          .map(afterText => stmtAdded.copy(text = stmtAdded.text + afterText))
          .getOrElse(stmtAdded)
      case _ => ctx
    }
  }
}

// handles argument transfer for 1 argument at the moment. Should be expanded if more arguments are needed
trait StatementManipulationVisitor extends PositionalAstFolder[AstEdit] {
  def addition: (String, String)

  val pattern = new Regex("([^{]+)(\\{\\d+\\})?(.*)", "command", "arg", "afterCommand")

  val targetCommand = addition._1

  val addedCommand = pattern.findFirstMatchIn(addition._2)
    .map(_.group("command").stripSuffix(" ")).getOrElse(addition._2)
  val addedArgument = pattern.findFirstMatchIn(addition._2)
    .flatMap(m => Option(m.group("arg")))
    .map(_.drop(1).dropRight(1).toInt)
  val afterCommand = pattern.findFirstMatchIn(addition._2).map(_.group("afterCommand"))

  // AstPath.Stmt(-1) delimits that the whitespace here is actually being *inserted* and
  // is not original to the AST.
  def newWsMap(wsMap: WhitespaceMap, position: AstPath, stmt: Statement): WhitespaceMap =
    addedArgument.map { i =>
      val oldArgPosition = position / AstPath.Expression(stmt.args(i), i)
      val newArgPosition = position / AstPath.Stmt(-1) / AstPath.Expression(stmt.args(i), 0)
      val newPositions =
        wsMap.toMap.filter {
          case ((k, _), _) => oldArgPosition.isParentOf(k)
        }.map {
          case ((k, p), v) => (k.repath(oldArgPosition, newArgPosition), p) -> v
        }
      wsMap ++ new WhitespaceMap(newPositions)
    }.getOrElse(wsMap)

  def manipulate(formatter: Formatter, astNode: AstNode, position: AstPath, ctx: AstFormat): AstFormat

  override def visitStatement(stmt: Statement, position: AstPath)(implicit edits: AstEdit): AstEdit = {
    if (stmt.command.token.text.equalsIgnoreCase(targetCommand)) {
      val repositionedWs =
        new WhitespaceMap(
          edits.wsMap.toMap
            .filter { case ((k, _), _) => k == position }
            .map { case ((k, p), v) => (k.repath(position, position / AstPath.Stmt(-1)), p) -> v }
          )
      super.visitStatement(stmt, position)(using
        edits
          .addOperation(position, manipulate)
          .copy(wsMap = newWsMap(edits.wsMap, position, stmt) ++ repositionedWs))
    } else
      super.visitStatement(stmt, position)
  }
}
