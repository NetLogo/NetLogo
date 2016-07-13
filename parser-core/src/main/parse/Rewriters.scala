// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstFolder, AstNode, CommandBlock, ReporterApp, ReporterBlock, Statement, prim },
  prim._unknowncommand

import Formatter.Operation
import WhiteSpace._

import scala.util.matching.Regex

object NoopFolder extends PositionalAstFolder[Map[AstPath, Operation]] {}

class RemovalVisitor(droppedCommand: String) extends PositionalAstFolder[Map[AstPath, Operation]] {

  def delete(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context = ctx

  override def visitStatement(stmt: Statement, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(droppedCommand))
      super.visitStatement(stmt, position)(ops + (position -> delete _))
    else
      super.visitStatement(stmt, position)
  }
}

class ReplaceReporterVisitor(alteration: (String, String)) extends PositionalAstFolder[Map[AstPath, Operation]] {
  import Formatter._

  def replace(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Context): Context = {
    astNode match {
      case app: ReporterApp =>
        ctx.copy(text = ctx.text + ctx.wsMap.leading(path) + alteration._2)
      case _ => ctx
    }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
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
          text = c1.text + ctx.wsMap.trailing(position),
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
  extends PositionalAstFolder[Map[AstPath, Operation]] {
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

  def newWsMap(ctx: Context, position: AstPath, stmt: Statement): WhitespaceMap =
    addedArgument.map { i =>
      val oldArgPosition = position / AstPath.Expression(stmt.args(i), i)
      val newArgPosition = position / AstPath.Expression(stmt.args(i), 0)
      ctx.wsMap.map {
        case ((k, p), v) =>
          if (oldArgPosition.isParentOf(k))
            (k.repath(oldArgPosition, newArgPosition), p) -> v
          else
            (k, p) -> v
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

class Lambdaizer extends PositionalAstFolder[Map[AstPath, Operation]] {
  import prim.{ _commandlambda, _commandtask, _reporterlambda, _reportertask, _task, _taskvariable }

  def replaceVar(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case app: ReporterApp =>
        app.reporter match {
          case tv: _taskvariable => ctx.appendText(ctx.wsMap.leading(path) + s"_${tv.vn}")
          case _ => ctx
        }
      case _ => ctx
    }
  }

  def addVariables(maxVar: Option[Int])(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp =>
        val visitBody = formatter.visitExpression(app.args(0), path, 0)(ctx.copy(text = ""))
        val bodyText = visitBody.text.replaceFirst("\\[", "").reverse.replaceFirst("\\]", "").reverse
        app.reporter match {
          case rl: _reporterlambda if rl.synthetic && maxVar.isEmpty => ctx.appendText(ctx.wsMap.leading(path) + bodyText)
          case cl: _commandlambda  if cl.synthetic && maxVar.isEmpty => ctx.appendText(ctx.wsMap.leading(path) + bodyText)
          case _ =>
            val vars = maxVar.map(1 to _).map(_.map(num => s"_$num")) getOrElse Seq()
            val varString = if (vars.nonEmpty) vars.mkString("[", " ", "] -> ") else ""
            ctx.appendText(ctx.wsMap.leading(path) + "[" + varString + bodyText + "]")
        }
      case _ => ctx
    }

  def onlyFirstArg(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp => formatter.visitExpression(app.args(0), path, 0)(ctx)
      case _                => ctx
    }

  object MaxTaskVariable extends AstFolder[Option[Int]] {
    override def visitReporterApp(app: ReporterApp)(implicit maxVariable: Option[Int]): Option[Int] =
      app.reporter match {
        case tv: _taskvariable =>
          super.visitReporterApp(app)(maxVariable.map(_ max tv.vn) orElse Some(tv.vn))
        // avoid crossing into another task
        case (_: _commandtask | _: _reportertask | _: _commandlambda | _: _reporterlambda) => maxVariable
        case _ => super.visitReporterApp(app)
      }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
    app.reporter match {
      case (_: _reporterlambda | _: _commandlambda) =>
        val variables = MaxTaskVariable.visitExpression(app.args(0))(None)
        super.visitReporterApp(app, position)(ops + (position -> addVariables(variables)))
      case _: _task           => super.visitReporterApp(app, position)(ops + (position -> onlyFirstArg _))
      case _: _taskvariable   => super.visitReporterApp(app, position)(ops + (position -> replaceVar _))
      case _                  => super.visitReporterApp(app, position)
    }
  }
}
