// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstFolder, AstNode, Command, CommandBlock, Dialect, Instruction, Reporter, ReporterApp, ReporterBlock, Statement, Syntax, TokenMapperInterface, prim },
  prim.{ _commandlambda, _lambdavariable, _reporterlambda, Lambda }

import Formatter.Operation
import WhiteSpace._

import LambdaTokenMapper._

// this should only be used with LambdaTokenMapper, below, which maps `?*` to special variables and
// makes `task` a reporter.
class Lambdaizer extends PositionalAstFolder[Map[AstPath, Operation]] {

  def varName(nestingDepth: Int, vn: Int): String = {
    val prefix = "?" * (nestingDepth + 1)
    s"$prefix$vn"
  }

  class ReplaceVar(nestingDepth: Int) extends Formatter.Operation {
    def apply(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
      astNode match {
        case app: ReporterApp =>
          app.reporter match {
             case tv: _taskvariable =>
               ctx.appendText(ctx.wsMap.leading(path) + varName(nestingDepth, tv.vn))
            case _ => ctx
          }
            case _ => ctx
      }
  }

  class AddVariables(maxVar: Option[Int], nestingDepth: Int) extends Formatter.Operation {
    def apply(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp =>
        val visitBody = formatter.visitExpression(app.args(0), path, 0)(ctx.copy(text = ""))
        val bodyText =
          if (app.reporter.isInstanceOf[_commandlambda])
            visitBody.text.replaceFirst("\\[", "").reverse.replaceFirst("\\]", "").reverse
          else
            visitBody.text
        app.reporter match {
          case rl: _reporterlambda if rl.synthetic && maxVar.isEmpty => ctx.appendText(ctx.wsMap.leading(path) + bodyText)
          case cl: _commandlambda  if cl.synthetic && maxVar.isEmpty => ctx.appendText(ctx.wsMap.leading(path) + bodyText)
          case _ =>
            val vars = maxVar.map(1 to _).map(_.map(num => varName(nestingDepth, num))) getOrElse Seq()
            val varString = if (vars.nonEmpty) vars.mkString(" [", " ", "] ->") else ""
            val frontMargin = if (varString.isEmpty || bodyText.startsWith(" ")) "" else " "
            val backMargin = {
              val storedMargin = ctx.wsMap.get(path, WhiteSpace.BackMargin)
              val hasVars = varString.nonEmpty
              storedMargin match {
                case None if ! hasVars || bodyText.endsWith(" ") => ""
                case Some(margin) if ! hasVars                   => margin
                case Some(margin) if ! margin.endsWith(" ")      => margin + " "
                case None                                        => " "
                case Some(margin)                                => margin
              }
            }
            ctx.appendText(ctx.wsMap.leading(path) + "[" + varString + frontMargin + bodyText + backMargin + "]")
        }
      case _ => ctx
    }
  }

  def onlyFirstArg(formatter: Formatter, astNode: org.nlogo.core.AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp =>
        val leadingWhitespace = ctx.wsMap.leading(path)
        formatter.visitExpression(app.args(0), path, 0)(
          ctx.copy(wsMap = ctx.wsMap.updated(path / AstPath.RepArg(0),  WhiteSpace.Leading, leadingWhitespace)))
      case _                => ctx
    }

  def wrapConciseForClarity(taskPosition: AstPath)(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp =>
        val leading = ctx.appendText(ctx.wsMap.leading(taskPosition) + "[ [] ->")
        val body = formatter.visitExpression(app.args(0), path, 0)(leading)
        if (body.text.endsWith(" "))
          body.appendText("]")
        else
          body.appendText(" ]")
      case _                => ctx
    }

  def wrapAmbiguousBlock(taskPosition: AstPath)(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context =
    astNode match {
      case app: ReporterApp =>
        val visitBody = formatter.visitExpression(app.args(0), path, 0)(ctx.copy(text = ""))
        val bodyText = visitBody.text.replaceFirst("\\[", "").reverse.replaceFirst("\\]", "").reverse
        val frontMargin = if (bodyText.startsWith(" ")) "" else " "
        val backMargin = if (bodyText.endsWith(" ")) "" else " "
        ctx.appendText(ctx.wsMap.leading(taskPosition) + "[ [] ->" + frontMargin + bodyText + backMargin + "]")
      case _ => ctx
    }

  object MaxTaskVariable extends AstFolder[Option[Int]] {
    override def visitReporterApp(app: ReporterApp)(implicit maxVariable: Option[Int]): Option[Int] =
      app.reporter match {
        case tv: _taskvariable =>
          super.visitReporterApp(app)(maxVariable.map(_ max tv.vn) orElse Some(tv.vn))
        // avoid crossing into another task
        case (_: _commandlambda | _: _reporterlambda) => maxVariable
        case _ => super.visitReporterApp(app)
      }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
    def wrapTaskBlockArgument(lambdaApp: ReporterApp): Operation = {
      val variables = MaxTaskVariable.visitExpression(lambdaApp.args(0))(None)
      if (variables.headOption.exists(_ > 0))
        onlyFirstArg _
      else
        wrapAmbiguousBlock(position)
    }

    app.reporter match {
      case r: _reporterlambda if r.argumentNames.nonEmpty => super.visitReporterApp(app, position)
      case c: _commandlambda  if c.argumentNames.nonEmpty => super.visitReporterApp(app, position)
      case (_: _reporterlambda | _: _commandlambda) =>
        val variables = MaxTaskVariable.visitExpression(app.args(0))(None)
        val nestingDepth = ops.filter { case (k, v) => k.isParentOf(position) && v.isInstanceOf[AddVariables] }.size
        super.visitReporterApp(app, position)(ops + (position -> new AddVariables(variables, nestingDepth)))
      case _: _task           =>
        // need to check if arg0 is synthetic. If it is synthetic, we need to make it an actual block
        app.args(0) match {
          case ra@ReporterApp(l@Lambda(args, true, _), _, _) if ! args.isEmpty =>
            // this case makes sure `let foo task crt` get converted to `let foo [[?1] -> foo ?1]`
            val nestingDepth = ops.filter { case (k, v) => k.isParentOf(position) && v.isInstanceOf[AddVariables] }.size
            val printLambdaVars = new ConvertConciseVariables(nestingDepth)
            val printOps = printLambdaVars.visitExpression(ra.args(0), position / AstPath.RepArg(0), 0)(ops)
            super.visitReporterApp(app, position)(printOps + (position -> new AddVariables(Some(args.length), nestingDepth)))
          case ReporterApp(Lambda(_, true, _), _, _) =>
            // this case makes sure `let foo task pi` doesn't get converted to `let foo pi`
            super.visitReporterApp(app, position)(ops + (position -> wrapConciseForClarity(position)))
          case lambdaApp@ReporterApp(_reporterlambda(args, _, _), _, _) if args.isEmpty =>
            // This case makes sure `let foo task [pi]` doesn't get converted to `let foo [pi]`
            super.visitReporterApp(app, position)(ops + (position -> wrapTaskBlockArgument(lambdaApp)))
          case lambdaApp@ReporterApp(_commandlambda(args, false, _), _, _) if args.isEmpty =>
            // This case makes sure `let foo task [tick]` doesn't get converted to `let foo [tick]`
            super.visitReporterApp(app, position)(ops + (position -> wrapTaskBlockArgument(lambdaApp)))
          case _ =>
            super.visitReporterApp(app, position)(ops + (position -> onlyFirstArg _))
        }
      case _: _taskvariable   =>
        val nestingDepth = ops.filter { case (k, v) => k.isParentOf(position) && v.isInstanceOf[AddVariables] }.size
        super.visitReporterApp(app, position)(ops + (position -> new ReplaceVar(nestingDepth - 1)))
      case _                  => super.visitReporterApp(app, position)
    }
  }
}

class ConvertConciseVariables(nesting: Int) extends PositionalAstFolder[Map[AstPath, Operation]] {
  def varName(nestingDepth: Int, vn: Int): String = {
    val prefix = "?" * (nestingDepth + 1)
    s"$prefix${vn + 1}"
  }

  def showConciseVariable(formatter: Formatter, astNode: AstNode, path: AstPath, ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case ReporterApp(_lambdavariable(name, true), _, _) =>
        val convertedTaskVariableName = varName(nesting, name.tail.toInt)
        val leadingSpace = if (ctx.text.endsWith(" ")) "" else " "
        ctx.appendText(leadingSpace + convertedTaskVariableName + " ")
    }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit ops: Map[AstPath, Operation]): Map[AstPath, Operation] = {
    app.reporter match {
      case _lambdavariable(name, true) => ops + (position -> showConciseVariable _)
      case (_: _reporterlambda | _: _commandlambda) => ops // don't go further down
      case _ => super.visitReporterApp(app, position)(ops)
    }
  }
}

object LambdaTokenMapper {
  case class _taskvariable(vn: Int) extends Reporter {
    override def syntax = Syntax.reporterSyntax(ret = Syntax.WildcardType)
    override def toString = s"_taskvariable($vn)"
  }
  case class _task() extends Reporter {
    override def syntax = {
      val anyTask = Syntax.CommandType | Syntax.ReporterType
      Syntax.reporterSyntax(right = List(anyTask), ret = anyTask)
    }
  }
}

class LambdaConversionDialect(delegate: Dialect) extends Dialect {
  def is3D = delegate.is3D
  def agentVariables = delegate.agentVariables
  val tokenMapper = new LambdaTokenMapper(delegate.tokenMapper)
}

class LambdaTokenMapper(delegate: TokenMapperInterface) extends TokenMapperInterface {

  val taskReporters = Map[String, () => Reporter](
    "TASK" -> (() => _task()),
    "?"    -> (() => _taskvariable(1))
  ) ++ (1 to 9).map(i => s"?$i" -> (() => _taskvariable(i))).toMap

  def getCommand(s: String): Option[Command] = delegate.getCommand(s)
  def getReporter(s: String): Option[Reporter] = taskReporters.get(s).map(_.apply()) orElse delegate.getReporter(s)
  def breedInstruction(primName: String, breedName: String): Option[Instruction] =
    delegate.breedInstruction(primName, breedName)

  def allCommandNames: Set[String]  = delegate.allCommandNames
  def allReporterNames: Set[String] = taskReporters.keySet ++ delegate.allReporterNames
}
