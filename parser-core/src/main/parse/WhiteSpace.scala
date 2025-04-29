// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ prim, CommandBlock, ProcedureDefinition,
  ReporterApp, ReporterBlock, SourceLocatable, SourceLocation, Statement, Token, TokenType, TokenizerInterface },
  prim.{ _commandlambda, _const, _constcodeblock, _reporterlambda }

import scala.collection.BufferedIterator

object WhiteSpace {
  sealed trait Placement { def default: String }
  case object Leading     extends Placement { val default = "" }
  case object Trailing    extends Placement { val default = "" }
  case object BackMargin  extends Placement { val default = " " }
  case object FrontMargin extends Placement { val default = "" }
  case object Content     extends Placement { val default = "" }

  object Context {
    def empty = new Context(WhitespaceMap.empty, None, Map(), None)
    def empty(filename: String) = new Context(WhitespaceMap.empty, None, Map(), Some(AstPath() -> SourceLocation(0, 0, filename)))
    def empty(pos: Option[(AstPath, SourceLocation)]) = new Context(WhitespaceMap.empty, None, Map(), pos)
  }

  case class Context(
    whitespaceLog: WhitespaceMap,
    lastToken: Option[Token] = None,
    tokenIterators: Map[String, BufferedIterator[Token]] = Map(),
    lastPosition: Option[(AstPath, SourceLocation)] = None,
    isAnonymousProcedureBlock: Boolean = false) {
      val whitespaceMap = whitespaceLog.toMap

      def addWhitespace(path: AstPath, placement: Placement, newWhiteSpace: String): Context =
        copy(whitespaceLog = whitespaceLog.updated(path, placement, newWhiteSpace))

      def addLeadingWhitespace(path: AstPath, ws: String, location: SourceLocation): Context =
        addLeadingWhitespace(path, ws).copy(lastPosition = Some((path, location)))

      def addLeadingWhitespace(path: AstPath, ws: String): Context =
        copy(whitespaceLog = whitespaceLog.addWhitespace(path, Leading, ws))

      def addTokenIterator(filename: String, toks: BufferedIterator[Token]): Context =
        copy(tokenIterators = tokenIterators + (filename -> toks))

      def updatePosition(path: AstPath, location: SourceLocation): Context =
        copy(lastPosition = lastPosition.map(t => (path, location)))

      def lastPath: Option[AstPath] = lastPosition.map(_._1)

      def enterAnonymousProcedure: Context =
        copy(isAnonymousProcedureBlock = true)

      def exitAnonymousProcedure: Context =
        copy(isAnonymousProcedureBlock = false)

      def debugPrint(s: String): Context = {
        println(s + ":\n" + whitespaceLog.toMap.mkString(s"\n"))
        this
      }

      def seq(fs: Context => Context*): Context =
        fs.foldLeft(this) { case (c, f) => f(c) }

      def through[A](f: Context => (A, Context), g: A => Context => Context): Context = {
        val (a, c1) = f(this)
        g(a)(c1)
      }
    }

  case class ProcedureWhiteSpace(leading: String, internal: Map[AstPath, WhiteSpace], trailing: String)

  class Tracker(getSource: String => String, tokenizer: TokenizerInterface) extends PositionalAstFolder[Context] {
    import AstPath._

    override def visitProcedureDefinition(proc: ProcedureDefinition)(c: Context): Context = {
      val path = AstPath(Proc(proc.procedure.name))

      def tagFunctionHeader(c: Context): Context = {
        proc.procedure.argTokens.lastOption match {
          case None =>
            val loc = proc.procedure.nameToken.sourceLocation
            tagLeadingWhitespace(path, loc.copy(start = loc.end))(c)
          case Some(lastArg) =>
            val (toks, c1) = tokensToPoint(lastArg.filename, lastArg.end)(c)
            val iter = c1.tokenIterators(proc.filename)
            val tokBuffer = toks.toBuffer
            while (iter.head.tpe != TokenType.CloseBracket && iter.head.tpe != TokenType.Eof) {
              tokBuffer += iter.next()
            }
            if (iter.head.tpe == TokenType.CloseBracket)
              tokBuffer += iter.next()
            val allToks = tokBuffer.toSeq
            c1.addLeadingWhitespace(path, allToks.map(_.text).mkString(""), allToks.last.sourceLocation)
        }
      }

      c.seq(
        tagFunctionHeader _,
        super.visitProcedureDefinition(proc)(_)
      ).through(
          sourceToPoint(proc.filename, proc.end),
          (backMargin: String) => (c1: Context) =>
            c1.lastPosition.map(p => c1.addWhitespace(p._1, Trailing, backMargin))
              .getOrElse(c1)
              .addWhitespace(path, BackMargin, backMargin)
      ).through(
          sourceToPoint(proc.filename, proc.end + 3),
          ((trailing: String) =>
              _.addWhitespace(path, Trailing, trailing)
               .updatePosition(path, proc.sourceLocation.copy(end = proc.end + 3))))
    }

    override def visitReporterApp(app: ReporterApp, path: AstPath)(implicit c: Context): Context = {
      def reporterSourceLocation(a: ReporterApp) =
        SourceLocation(a.start max a.reporter.token.start, a.reporter.token.end, a.filename)

      if (app.reporter.syntax.isInfix) {
        c.exitAnonymousProcedure.seq(
          visitExpression(app.args.head, path, 0)(_),
          tagLeadingWhitespace(path, app.reporter.token.sourceLocation),
          app.args.zipWithIndex.tail.foldLeft(_) {
            case (ctx, (arg, i)) => visitExpression(arg, path, i)(ctx)
          })
      } else if (app.reporter.isInstanceOf[_reporterlambda]) {
        c.enterAnonymousProcedure.seq(
          tagLeadingWhitespace(path, app.reporter.token.sourceLocation),
          arrowFrontMargin(app, path),
          super.visitReporterApp(app, path)(_),
          tagTrailingWhitespace(app, path)).exitAnonymousProcedure
      } else if (app.reporter.isInstanceOf[_commandlambda] && app.reporter.asInstanceOf[_commandlambda].argumentNames.nonEmpty) {
        c.enterAnonymousProcedure.seq(
          tagLeadingWhitespace(path, app.reporter.token.sourceLocation),
          arrowFrontMargin(app, path),
          super.visitReporterApp(app, path)(_),
          tagTrailingWhitespace(app, path)).exitAnonymousProcedure
      } else if (app.reporter.isInstanceOf[_commandlambda]) {
        c.enterAnonymousProcedure.seq(
          tagLeadingWhitespace(path, reporterSourceLocation(app)),
          super.visitReporterApp(app, path)(_))
      } else if (app.reporter.isInstanceOf[_const]) {
        c.through(
          sourceToPoint(app.filename, app.start, _ => true),
          (leading: String) => _.addLeadingWhitespace(path, leading, app.sourceLocation)
        ).through(
          sourceToPoint(app.filename, app.end, _ => true),
          (content: String) => _.addWhitespace(path, Content, content))
      } else if (app.reporter.isInstanceOf[_constcodeblock]) {
        c.exitAnonymousProcedure.through(
          sourceToPoint(app.filename, app.start, _ => true),
          (leading: String) => _.addLeadingWhitespace(path, leading, app.sourceLocation)
        ).through(
          sourceToPoint(app.filename, app.end, _ => true),
          (content: String) => _.addWhitespace(path, Content, content))
      } else {
        c.exitAnonymousProcedure.seq(
          tagLeadingWhitespace(path, reporterSourceLocation(app)),
          super.visitReporterApp(app, path)(_))
      }
    }

    private def arrowFrontMargin(app: ReporterApp, path: AstPath)(c: Context): Context = {
      val (ts, c1) = tokensToPoint(app.filename, app.end)(c)
      ts.find(_.text == "->") match {
        case Some(arrowToken) =>
          val (frontMargin, rest) = ts.span(_.text != "->")
          val afterArrow = rest.tail
          c1.addWhitespace(path, FrontMargin, (frontMargin :+ arrowToken).map(_.text).mkString(""))
            .updatePosition(path, arrowToken.sourceLocation)
            .copy(tokenIterators = c1.tokenIterators.updated(app.filename, (afterArrow.iterator ++ c1.tokenIterators(app.filename)).buffered))
        case None =>
          c1.copy(tokenIterators = c1.tokenIterators.updated(app.filename, (ts.iterator ++ c1.tokenIterators(app.filename)).buffered))
      }
    }
    override def visitStatement(stmt: Statement, path: AstPath)(implicit c: Context): Context = {
      def statementStart(s: Statement) = s.start max s.command.token.start
      c.seq(
        tagLeadingWhitespace(path, stmt.command.token.sourceLocation.copy(start = statementStart(stmt))),
        super.visitStatement(stmt, path)(_))
    }

    override def visitCommandBlock(blk: CommandBlock, path: AstPath)(implicit c: Context): Context = {
      val (leadBracket, tailBracket) = if (blk.synthetic || c.isAnonymousProcedureBlock) ("", "") else ("[", "]")
      c.seq(
        tagLeadingWhitespace(path, SourceLocation(blk.start, blk.start, blk.filename), leadBracket),
        removeOpenBracket(blk) _,
        (c2) => super.visitCommandBlock(blk, path)(c2.exitAnonymousProcedure),
        tagTrailingWhitespace(blk, path, tailBracket)(_))
    }

    override def visitReporterBlock(blk: ReporterBlock, path: AstPath)(implicit c: Context): Context = {
      val (leadBracket, tailBracket) = if (c.isAnonymousProcedureBlock) ("", "") else ("[", "]")
      c.seq(
        tagLeadingWhitespace(path, SourceLocation(blk.start, blk.start, blk.filename), leadBracket),
        removeOpenBracket(blk) _,
        (c2) => super.visitReporterBlock(blk, path)(c2),
        tagTrailingWhitespace(blk, path, tailBracket)(_))
    }

    private def removeOpenBracket(blk: SourceLocatable)(c: Context): Context = {
      if (c.tokenIterators(blk.filename).head.tpe == TokenType.OpenBracket)
        c.tokenIterators(blk.filename).next()
      c
    }

    private def tagTrailingWhitespace(locatable: SourceLocatable, path: AstPath, extra: String = "")(c: Context): Context =
      c.lastPosition.map { p =>
        c.through(
          sourceToPoint(locatable.filename, locatable.end),
          ((backSpace: String) =>
              _.addWhitespace(path, BackMargin, backSpace + extra)
                .addWhitespace(p._1, Trailing, backSpace)
                .updatePosition(path, locatable.sourceLocation)))
      }.getOrElse(c)

    private def tagLeadingWhitespace(path: AstPath, location: SourceLocation, extra: String = "")(c: Context): Context =
      c.through(
        sourceToPoint(location.filename, location.start),
        (leading: String) => _.addLeadingWhitespace(path, leading + extra, location)
      ).seq(sourceToPoint(location.filename, location.end)(_)._2)

    private def notBracket(t: Token): Boolean =
      t.tpe != TokenType.OpenBracket && t.tpe != TokenType.CloseBracket

    // note this strips out bracket tokens
    private def sourceToPoint(
      filename: String,
      targetPoint: Int,
      includeOnly: Token => Boolean = notBracket _)(c: Context): (String, Context) = {
      val (toks, c1) = tokensToPoint(filename, targetPoint)(c)
      val source = toks.filter(includeOnly).map(_.text).mkString("")
      (source, c1)
    }

    private def tokensToPoint(filename: String, targetPoint: Int)(c: Context): (Seq[Token], Context) = {
      val c1 = ensureOpenIterator(filename)(c)
      val iter = c1.tokenIterators(filename)
      if (iter.head.sourceLocation.start > targetPoint)
        (Seq(), c1)
      else {
        val buffer = scala.collection.mutable.Buffer[Token]()
        while (iter.head.sourceLocation.start < targetPoint) {
          buffer += iter.next()
        }
        (buffer.toSeq, c1)
      }
    }

    private def ensureOpenIterator(filename: String)(c: Context): Context =
      c.tokenIterators.get(filename).map(_ => c) getOrElse {
        val iter = tokenizer.tokenizeWithWhitespace(getSource(filename), filename).buffered
        c.lastPosition.foreach { // Fast-forward to make sure we're up to the current path
          case (_, SourceLocation(_, currentEnd, _)) =>
            while (iter.head.start < currentEnd) { iter.next() }
        }
        c.addTokenIterator(filename, iter)
      }
  }
}

case class WhiteSpace(leading: String, trailing: String = "", backMargin: String = " ")

import WhiteSpace._

trait FormattingWhitespace {
  def get(path: AstPath, placement: Placement): Option[String]
  def leading(path: AstPath): String
  def frontMargin(path: AstPath): String
  def content(path: AstPath): String
  def backMargin(path: AstPath): String
  def trailing(path: AstPath): String
}

object WhitespaceMap {
  def empty = new WhitespaceMap(Map())
}

class WhitespaceMap(ws: Map[(AstPath, Placement), String]) extends FormattingWhitespace {
  def addWhitespace(path: AstPath, placement: Placement, s: String): WhitespaceMap =
    if (s != placement.default) copy(ws + ((path, placement) -> s))
    else this

  def updated(path: AstPath, placement: Placement, s: String): WhitespaceMap =
    copy(ws = ws + ((path , placement) -> s))

  def contains(path: AstPath, placement: Placement): Boolean =
    ws.contains(path -> placement)

  def get(path: AstPath, placement: Placement): Option[String] =
    ws.get(path -> placement)

  def leading(path: AstPath): String     = getOrDefault(path, Leading)
  def frontMargin(path: AstPath): String = getOrDefault(path, FrontMargin)
  def content(path: AstPath): String     = getOrDefault(path, Content)
  def backMargin(path: AstPath): String  = getOrDefault(path, BackMargin)
  def trailing(path: AstPath): String    = getOrDefault(path, Trailing)

  def getOrDefault(path: AstPath, placement: Placement): String =
    ws.getOrElse(path -> placement, placement.default)

  def ++(other: WhitespaceMap): WhitespaceMap =
    copy(ws ++ other.toMap)

  def -(p: (AstPath, Placement)): WhitespaceMap =
    copy(ws - p)

  def map(f: (((AstPath, Placement), String)) => ((AstPath, Placement), String)): WhitespaceMap =
    copy(ws.map(f))

  def copy(ws: Map[(AstPath, Placement), String]): WhitespaceMap = new WhitespaceMap(ws)

  def toMap: Map[(AstPath, Placement), String] = ws
}
