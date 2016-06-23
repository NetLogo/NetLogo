// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ prim, AstNode, CommandBlock, ProcedureDefinition,
  ReporterApp, ReporterBlock, Statement },
  prim._reportertask

object WhiteSpace {
  case class Context(
    astWsMap: Map[AstPath, WhiteSpace] = Map(),
    // file, ast position, integer offset
    lastPosition: Option[(String, AstPath, Int)] = None) {
      def addWhitespace(path: AstPath, ws: WhiteSpace, i: Int): Context = {
        val mapAdditions = lastPosition
          .flatMap(p => astWsMap.get(p._2)
          .map(existingWs => (p -> existingWs)))
          .map {
            case ((_, astPath, i), existingWs) =>
              Map(astPath -> existingWs.copy(trailing = ws.backMargin), path -> ws)
          }.getOrElse(Map(path -> ws))

        copy(astWsMap = astWsMap ++ mapAdditions,
          lastPosition = lastPosition.map(t => (t._1, path, i)))
      }

      def updateWhitespace(astPath: AstPath, f: WhiteSpace => WhiteSpace): Context = {
        astWsMap.get(astPath)
          .map(f)
          .map(updatedWs => copy(astWsMap = astWsMap.updated(astPath, updatedWs)))
          .getOrElse(this)
      }

      def addLeadingWhitespace(path: AstPath, ws: String): Context =
        copy(astWsMap = astWsMap + (path -> WhiteSpace(ws)))

      def addLeadingWhitespace(path: AstPath, ws: String, i: Int): Context =
        copy(lastPosition = lastPosition.map(t => (t._1, path, i)),
          astWsMap = astWsMap + (path -> WhiteSpace(ws)))

      def updatePosition(position: AstPath, offset: Int): Context =
        copy(lastPosition = lastPosition.map(t => (t._1, position, offset)))

      def lastOffset: Option[Int] = lastPosition.map(_._3)
      def lastPath: Option[AstPath] = lastPosition.map(_._2)
      def lastFile: Option[String] = lastPosition.map(_._1)
    }

  case class ProcedureWhiteSpace(leading: String, internal: Map[AstPath, WhiteSpace], trailing: String)

  class Tracker(getSource: String => String) extends PositionalAstFolder[Context] {
    import AstPath._

    def sourceFromLast[A <: AstNode](c: Context, astNode: A, getTargetPoint: A => Int): String =
      c.lastOffset.map(i => sourceSlice(astNode, i, getTargetPoint(astNode)))
        .getOrElse("")

    def sourceSlice(astNode: AstNode, start: Int, end: Int): String =
      getSource(astNode.file)
        .slice(start, end)
        .replaceAllLiterally("[", "")
        .replaceAllLiterally("]", "")

    override def visitProcedureDefinition(proc: ProcedureDefinition)(c: Context): Context = {
      val initialWs = sourceFromLast[ProcedureDefinition](c, proc, _.start)
      val functionHeaderEnd =
        proc.procedure.argTokens.lastOption.getOrElse(proc.procedure.nameToken).end
      val functionHeader = getSource(proc.file).slice(proc.start, functionHeaderEnd)
      val position = AstPath(Proc(proc.procedure.name))
      val procedureStart = initialWs + functionHeader
      val c1 = c.updatePosition(position, functionHeaderEnd)
      val c2 = super.visitProcedureDefinition(proc)(c1)
      val backMargin = sourceFromLast[ProcedureDefinition](c2, proc, _.end)
      val trailing = getSource(proc.file).slice(proc.end, proc.end + 3)

      c2.addWhitespace(position, WhiteSpace(procedureStart, trailing, backMargin), proc.end + 3)
    }

    override def visitReporterApp(app: ReporterApp, position: AstPath)(implicit c: Context): Context = {
      if (app.reporter.syntax.isInfix) {
        val c1 = visitExpression(app.args.head, position, 0)(c)
        val c2 = c1.addLeadingWhitespace(position,
          sourceFromLast[ReporterApp](c1, app, _ => app.reporter.token.start),
          app.reporter.token.end)
        app.args.zipWithIndex.tail.foldLeft(c2) {
          case (ctx, (arg, i)) => visitExpression(arg, position, i)(ctx)
        }
      } else if (app.reporter.isInstanceOf[_reportertask]) {
        def reporterStart(a: ReporterApp) = a.start max a.reporter.token.start
        val c1 =
          c.addLeadingWhitespace(position, sourceFromLast(c, app, reporterStart _), app.reporter.token.end)
        val c2 = super.visitReporterApp(app, position)(c1)
        c2.lastPosition.map(p =>
          c2.updateWhitespace(position, _.copy(backMargin = sourceSlice(app, p._3, app.end)))
            .updateWhitespace(p._2, _.copy(trailing = sourceSlice(app, p._3, app.end)))
            .updatePosition(position, app.end))
        .getOrElse(c2)
      } else {
        def reporterStart(a: ReporterApp) = a.start max a.reporter.token.start
        val c1 =
          c.addLeadingWhitespace(position, sourceFromLast(c, app, reporterStart _), app.reporter.token.end)
        super.visitReporterApp(app, position)(c1)
      }
    }

    override def visitStatement(stmt: Statement, position: AstPath)(implicit c: Context): Context = {
      def statementStart(s: Statement) = s.start max s.command.token.start
      val newCtxt =
        c.addLeadingWhitespace(position, sourceFromLast[Statement](c, stmt, statementStart _), stmt.command.token.end)
      super.visitStatement(stmt, position)(newCtxt)
    }

    override def visitCommandBlock(blk: CommandBlock, position: AstPath)(implicit c: Context): Context = {
      visitBlock(blk, position, ctx => super.visitCommandBlock(blk, position)(ctx))(c)
    }

    override def visitReporterBlock(blk: ReporterBlock, position: AstPath)(implicit c: Context): Context = {
      visitBlock(blk, position, ctx => super.visitReporterBlock(blk, position)(ctx))(c)
    }

    def visitBlock(blk: AstNode, position: AstPath, superCall: Context => Context)(implicit c: Context): Context = {
      val source = sourceFromLast[AstNode](c, blk, _.start)
      val beforeInternal =
        c.copy(astWsMap = c.astWsMap + (position -> WhiteSpace(source)))
          .updatePosition(position, blk.start)
      val afterInternal = superCall(beforeInternal)
      afterInternal.lastPosition.map(p =>
          afterInternal.updateWhitespace(position, _.copy(backMargin = sourceSlice(blk, p._3, blk.end)))
            .updateWhitespace(p._2, _.copy(trailing = sourceSlice(blk, p._3, blk.end)))
            .updatePosition(position, blk.end))
      .getOrElse(afterInternal)
    }
  }
}

case class WhiteSpace(leading: String, trailing: String = "", backMargin: String = " ")
