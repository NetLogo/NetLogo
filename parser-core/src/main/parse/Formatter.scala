// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.util.Locale

import org.nlogo.core.{ AstNode, CommandBlock, Dump, Instruction, LogoList, ProcedureDefinition,
  ReporterApp, ReporterBlock, Statement, prim },
  prim.{ _commandlambda, _const, _constcodeblock, _lambdavariable, _let, _letname, _multilet, _multiassignitem, _multiset, _set, Lambda }

object Formatter {

  def context(
    text:                String,
    operations:          Map[AstPath, AstFormat.Operation],
    instructionToString: Instruction => String = instructionString,
    wsMap:               FormattingWhitespace  = WhitespaceMap.empty): AstFormat =
      AstFormat(text, operations, instructionString, wsMap)

  private var skipNext: Boolean = false

  def instructionString(i: Instruction): String =
    i match {
      case _const(value) if value.isInstanceOf[LogoList]       => Dump.logoObject(value, true, false)
      case r: _const                                           => r.token.text
      case r: _commandlambda                                   => ""
      case v: _lambdavariable if v.synthetic                   => ""
      case l @ _let(_, Some(name))                             =>
        if (l.token.text != name) { s"${l.token.text} $name" } else { "" }
      case _: _letname                                         => ""
      case s: _set if s.token.text.toUpperCase(Locale.ENGLISH) != "SET"  => { skipNext = true; "" }
      case m: _multilet                                        => s"let ${m.letList}"
      case _: _multiassignitem                                 => ""
      case s: _multiset                                        => s"set ${s.setList}"
      case r                                                   =>
        if (skipNext) { skipNext = false; "" } else { r.token.text }
    }

  def deletedInstructionToString(i: Instruction): String = ""

  implicit class RichFormat(a: AstFormat) {
    def appendText(t: String): AstFormat =
      a.copy(text = a.text + t)
  }
}

class Formatter extends PositionalAstFolder[AstFormat] {
  import Formatter.RichFormat

  import Formatter.context

  override def visitProcedureDefinition(proc: ProcedureDefinition)(c: AstFormat): AstFormat = {
    val position = AstPath(AstPath.Proc(proc.procedure.name.toUpperCase(Locale.ENGLISH)))
    super.visitProcedureDefinition(proc)(c.appendText(c.wsMap.leading(position)))
      .appendText(c.wsMap.backMargin(position))
      .appendText(c.wsMap.trailing(position))
  }

  override def visitCommandBlock(block: CommandBlock, position: AstPath)(implicit c: AstFormat): AstFormat = {
    def beginSyntheticBlock(ws: FormattingWhitespace)(p: AstPath): String = ws.leading(p)
    def closeSyntheticBlock(ws: FormattingWhitespace)(p: AstPath): String = ws.backMargin(p)

    if (block.synthetic && block.statements.stmts.isEmpty)
      c.appendText(c.wsMap.leading(position))
    else if (block.synthetic)
      visitBlock(block, position, c1 => super.visitCommandBlock(block, position)(using c1),
        beginSyntheticBlock, closeSyntheticBlock)
    else
      visitBlock(block, position, c1 => super.visitCommandBlock(block, position)(using c1))
  }

  override def visitReporterBlock(block: ReporterBlock, position: AstPath)(implicit c: AstFormat): AstFormat = {
    visitBlock(block, position, c1 => super.visitReporterBlock(block, position)(using c1))
  }

  private def normalBeginBlock(ws: FormattingWhitespace)(p: AstPath): String = ws.leading(p)
  private def normalEndBlock(ws: FormattingWhitespace)(p: AstPath): String = ws.backMargin(p)

  private def visitBlock(block: AstNode, position: AstPath, visit: AstFormat => AstFormat,
    beginBlock: FormattingWhitespace => AstPath => String = normalBeginBlock,
    endBlock:   FormattingWhitespace => AstPath => String = normalEndBlock)
    (implicit c: AstFormat): AstFormat = {
    c.operations.get(position)
      .map(op => op(this, block, position, c.appendText(leadingWhitespace(position))))
      .getOrElse {
        visit(c.appendText(beginBlock(c.wsMap)(position))).appendText(endBlock(c.wsMap)(position))
      }
  }

  override def visitStatement(stmt: Statement, position: AstPath)(implicit c: AstFormat): AstFormat = {
    c.operations.get(position)
      .map(op => op(this, stmt, position, c))
      .getOrElse {
        val ws = leadingWhitespace(position)
        val newContext = c.appendText(ws + c.instructionToString(stmt.command))
        super.visitStatement(stmt, position)(using newContext).copy(instructionToString = c.instructionToString)
      }
  }

  override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit c: AstFormat): AstFormat = {
    c.operations.get(position)
      .map(op => op(this, app, position, c))
      .getOrElse {
        val ws = leadingWhitespace(position)
        (app.reporter.syntax.isInfix, app.reporter) match {
          case (true, i) =>
            val c2 = visitExpression(app.args.head, position, 0)(using c)
            app.args.zipWithIndex.tail.foldLeft(c2.appendText(ws + c.instructionToString(i))) {
              case (ctx, (arg, i)) => visitExpression(arg, position, i)(using ctx)
            }

          case (_, b: _constcodeblock) =>
            super.visitReporterApp(app, position)(using
              c.appendText(leadingWhitespace(position) + c.wsMap.content(position)))

          case (false, con: _const) =>
            super.visitReporterApp(app, position)(using
              c.appendText(leadingWhitespace(position) + c.wsMap.content(position)))

          case (false, l: Lambda) =>
            val frontPadding = if (c.text.lastOption.forall(_ == ' ')) "" else " "
            val body =
              super.visitReporterApp(app, position)(using context("", c.operations, wsMap = c.wsMap)).text
            val args = l.arguments match {
              case Lambda.NoArguments(true)        => "[ ->"
              case Lambda.NoArguments(false)       => "["
              case Lambda.ConciseArguments(_, _)   => ""
              case Lambda.UnbracketedArgument(t)   => s"[ ${t.text} ->"
              case Lambda.BracketedArguments(args) => s"[ [${args.map(_.text).mkString(" ")}] ->"
            }
            val arrowSpace = if (args.endsWith(">") && ! body.startsWith(" ")) " " else ""
            val backMargin = c.wsMap.backMargin(position)
            val close = l.arguments match {
              case Lambda.ConciseArguments(_, _) => ""
              case _                             => "]"
            }
            val (body2, backMargin2) = if (body.lastOption.contains(' ')) {
              val b2  = s"${body.stripTrailing} "
              val bm2 = if (backMargin.headOption.contains(' ')) { backMargin.drop(1) } else { backMargin }
              (b2, bm2)
            } else {
              (body, backMargin)
            }
            c.appendText(frontPadding + args + arrowSpace + body2 + backMargin2 + close)

          // The `_let` handles writing the `_letname` out, so nothing to do here.
          // The `_letname` gets erased on full compilations (as opposed to rewriting only)
          // so we can't rely on it.  -Jeremy B June 2022
          case (false, l: _letname) =>
            c

          case (false, reporter) =>
            super.visitReporterApp(app, position)(using c.appendText(ws + c.instructionToString(reporter)))
              .copy(instructionToString = c.instructionToString)
        }
      }
  }

  private def leadingWhitespace(path: AstPath)(implicit c: AstFormat): String = c.wsMap.leading(path)
}
