// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, AstTransformer, AstVisitor,
  CommandBlock, CompilationOperand, Dump, Expression, ExtensionManager,
  FrontEndInterface, FrontEndProcedure, Instruction,
  LogoList, ProcedureDefinition, Reporter, ReporterApp, ReporterBlock,
  Statement, Statements, StructureResults, Token, TokenizerInterface, TokenType }
import org.nlogo.core.prim.{ _commandtask, _const, _reportertask, _unknowncommand }

import scala.util.matching.Regex

class AstRewriter(tokenizer: TokenizerInterface, op: CompilationOperand) extends FrontEndInterface.SourceRewriter {
  type WhiteSpaceMap = Map[Seq[AstPath], String]

  private def preserveBody(structureResults: StructureResults, header: String, procedures: String): String = header + procedures

  def removeFirstArg(dropArgFrom: String): String = {
    rewrite(new FirstArgumentRemover(dropArgFrom), preserveBody _)
  }

  def remove(dropCommand: String): String = {
    rewrite(new RemovalVisitor(dropCommand), preserveBody _)
  }

  def addCommand(addCommand: (String, String)): String = {
    rewrite(new AddVisitor(addCommand), preserveBody _)
  }

  def replaceCommand(replaceCommand: (String, String)): String = {
    rewrite(new ReplaceVisitor(replaceCommand), preserveBody _)
  }

  def replaceReporter(replaceReporter: (String, String)): String = {
    rewrite(new ReplaceReporterVisitor(replaceReporter), preserveBody _)
  }

  def addExtension(newExtension: String): String = {
    rewrite(NoopFolder, declarationReplace("extensions", extensionsRegex, _.extensions.map(_.text), newExtension))
  }

  def addGlobal(newGlobal: String): String = {
    rewrite(NoopFolder, declarationReplace("globals", globalsRegex, _.program.userGlobals.map(_.toLowerCase), newGlobal))
  }

  private val extensionsRegex = new Regex("(?i)(?m)extensions\\s+\\[[^]]*\\]")
  private val globalsRegex = new Regex("(?i)(?m)globals\\s+\\[[^]]*\\]")

  private def declarationReplace(
    declKeyword: String,
    declRegex: Regex,
    declItems: StructureResults => Seq[String],
    addedItem: String)(
    res: StructureResults, headers: String, procedures: String): String = {
    val newDecl =
      declKeyword + " " + (declItems(res) :+ addedItem).distinct.mkString("[", " ", "]")
    val modifiedHeaders = declRegex
      .findFirstMatchIn(headers)
      .map(m => headers.take(m.start) + newDecl + headers.drop(m.end))
      .getOrElse(newDecl+ "\n" + headers)
    modifiedHeaders + procedures
  }

  def rewrite(
    visitor: PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]],
    wholeFile: (StructureResults, String, String) => String): String = {
    val structureResults = StructureParser.parseSources(tokenizer, op)
    val globallyUsedNames =
      StructureParser.usedNames(structureResults.program, op.oldProcedures ++ structureResults.procedures)
    val procs = structureResults.procedures.values.map(parseProcedure(structureResults, globallyUsedNames))

    val (wsMap, fileHeaders) = trackWhitespace(op.sources, procs)

    val operations = procs.foldLeft(Map[Seq[AstPath], Formatter.Operation]()) {
      case (dels, proc) => visitor.visitProcedureDefinition(proc)(dels)
    }

    val wsRegex = new Regex("(?m)\\s+$").unanchored

    val rewritten =
      wholeFile(structureResults, fileHeaders.getOrElse("", ""), format(operations, wsMap, procs))
    val wsStripped = wsRegex.replaceAllIn(rewritten, "")
    wsStripped
  }

  def trackWhitespace(sources: Map[String, String], procs: Iterable[ProcedureDefinition]): (Map[String, (String, WhiteSpaceMap, String)], Map[String, String]) = {
    val toRegex = new Regex("(?i)(?:\\bto\\b|\\bto-report\\b)")
    val ws = new WhiteSpaceTracker(sources)
    var fileHeaders: Map[String, String] = Map()
    val whiteSpaces =
      procs.foldLeft(
        (Map[String, (String, WhiteSpaceMap, String)](),
          WhiteSpaceTracker.Context(Some(0), Map(), lastFile = None))) {
      case ((procWhitespaceMap, ctx), proc) =>
        val newContext =
          if (ctx.lastFile != Some(proc.file)) {
            val procStart = toRegex.findFirstMatchIn(sources(proc.file)).map(_.start).getOrElse(0)
            fileHeaders = (fileHeaders + (proc.file -> sources(proc.file).slice(0, procStart)))
            WhiteSpaceTracker.Context(lastEnd = Some(procStart), Map(), lastFile = Some(proc.file))
          } else
            WhiteSpaceTracker.Context(ctx.lastEnd, Map(), ctx.lastFile)
        val r = ws.visitProcedureDefinition(proc)(newContext)
        (procWhitespaceMap + (proc.procedure.name -> ((r.initialWs, r.astWsMap, r.finalWs))), r)
    }
    (whiteSpaces._1, fileHeaders)
  }

  def format(
    operations: Map[Seq[AstPath], Formatter.Operation],
    wsMap: Map[String, (String, WhiteSpaceMap, String)],
    procs: Iterable[ProcedureDefinition]): String = {
    val formatter = new Formatter
    val res = procs.foldLeft(Formatter.Context("", operations)) {
      case (acc, proc) =>
        val (leadingWs, procWsMap, trailingWs) = wsMap(proc.procedure.name)
        val r = formatter.visitProcedureDefinition(proc)(
          acc.copy(
            text = acc.text + leadingWs,
            instructionToString = Formatter.instructionString _,
            wsMap = procWsMap))
        r.copy(text = r.text + trailingWs)
    }
    res.text
  }

  def parseProcedure(structureResults: StructureResults, globallyUsedNames: Map[String, SymbolType])(procedure: FrontEndProcedure): ProcedureDefinition = {
    import op.extensionManager
    val rawTokens = structureResults.procedureTokens(procedure.name)
    val usedNames = globallyUsedNames ++ procedure.args.map(_ -> SymbolType.LocalVariable)
    // on LetNamer vs. Namer vs. LetScoper, see comments in LetScoper
    val namedTokens = {
      val letNamedTokens = LetNamer(rawTokens.iterator)
      val namer =
        new Namer(structureResults.program,
          op.oldProcedures ++ structureResults.procedures,
          extensionManager)
      val namedTokens = namer.process(letNamedTokens, procedure)
      val letScoper = new LetScoper(usedNames)
      letScoper(namedTokens.buffered)
    }
    val toks = namedTokens.toSeq
    ExpressionParser(procedure, toks.iterator)
  }
}

object NoopFolder extends PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]] {}

class FirstArgumentRemover(dropArgFrom: String) extends PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]] {
  import AstPath._

  def delete(formatter: Formatter, astNode: AstNode, path: Seq[AstPath], ctx: Formatter.Context): Formatter.Context = ctx

  override def visitReporterApp(app: ReporterApp, position: Seq[AstPath])(implicit ops: Map[Seq[AstPath], Formatter.Operation]): Map[Seq[AstPath], Formatter.Operation] = {
    if (app.reporter.token.text.equalsIgnoreCase(dropArgFrom) && app.args.length >= 1)
      super.visitReporterApp(app, position)(
        ops + ((position :+ AstPath.Expression(app.args.head, 0)) -> delete _))
    else
      super.visitReporterApp(app, position)
  }

  override def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit ops: Map[Seq[AstPath], Formatter.Operation]): Map[Seq[AstPath], Formatter.Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(dropArgFrom) && stmt.args.length >= 1)
      super.visitStatement(stmt, position)(
        ops + ((position :+ AstPath.Expression(stmt.args.head, 0)) -> delete _))
    else
      super.visitStatement(stmt, position)
  }
}

class RemovalVisitor(droppedCommand: String) extends PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]] {

  def delete(formatter: Formatter, astNode: AstNode, path: Seq[AstPath], ctx: Formatter.Context): Formatter.Context = ctx

  override def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit ops: Map[Seq[AstPath], Formatter.Operation]): Map[Seq[AstPath], Formatter.Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(droppedCommand))
      super.visitStatement(stmt, position)(ops + (position -> delete _))
    else
      super.visitStatement(stmt, position)
  }
}

class ReplaceReporterVisitor(alteration: (String, String)) extends PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]] {
  import Formatter._

  def replace(formatter: Formatter, astNode: AstNode, path: Seq[AstPath], ctx: Context): Context = {
    astNode match {
      case app: ReporterApp =>
        ctx.copy(text = ctx.text + ctx.wsMap(path) + alteration._2)
      case _ => ctx
    }
  }

  override def visitReporterApp(app: ReporterApp, position: Seq[AstPath])(implicit ops: Map[Seq[AstPath], Operation]): Map[Seq[AstPath], Formatter.Operation] = {
    if (app.reporter.token.text.equalsIgnoreCase(alteration._1))
      super.visitReporterApp(app, position)(ops + (position -> replace _))
    else
      super.visitReporterApp(app, position)
  }
}

class AddVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: Seq[AstPath], ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case stmt: Statement =>
        val AstPath.Stmt(id) = position.last
        val newCmd = new _unknowncommand(stmt.command.syntax)
        val newToken = stmt.command.token.refine(newPrim = newCmd, text = addedCommand)
        val newArgs = addedArgument.map(id => Seq(stmt.args(id))).getOrElse(Seq())
        val newStmt = stmt.copy(command = newCmd, args = newArgs)
        val c1 =
          formatter.visitStatement(newStmt, position)(ctx.copy(
            text = ctx.text + " ", operations = ctx.operations - position, wsMap = newWsMap(ctx, position, stmt)))
        val c2 =
          formatter.visitStatement(stmt, position)(c1.copy(text = c1.text + " ", operations = ctx.operations - position, wsMap = ctx.wsMap))
        c2
      case _               => ctx
    }
  }
}

// handles argument transfer for 1 argument at the moment. Should be expanded if more arguments are needed
class ReplaceVisitor(val addition: (String, String)) extends StatementManipulationVisitor {
  override def manipulate(formatter: Formatter, astNode: AstNode, position: Seq[AstPath], ctx: Formatter.Context): Formatter.Context = {
    astNode match {
      case stmt: Statement =>
        val newToken = stmt.command.token.refine(newPrim = stmt.command, text = addedCommand)
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
  extends PositionalAstFolder[Map[Seq[AstPath], Formatter.Operation]] {
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

  def newWsMap(ctx: Context, position: Seq[AstPath], stmt: Statement): Map[Seq[AstPath], String] =
    addedArgument.map { i =>
      val oldArgPosition = position :+ AstPath.Expression(stmt.args(i), i)
      val newArgPosition = position :+ AstPath.Expression(stmt.args(i), 0)
      ctx.wsMap.map {
        case (k, v) =>
          if (k.take(oldArgPosition.length) == oldArgPosition)
            (newArgPosition ++ k.drop(oldArgPosition.length)) -> v
          else
            k -> v
      }
    }.getOrElse(ctx.wsMap)

 def manipulate(formatter: Formatter, astNode: AstNode, position: Seq[AstPath], ctx: Context): Context

  override def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit ops: Map[Seq[AstPath], Operation]): Map[Seq[AstPath], Operation] = {
    if (stmt.command.token.text.equalsIgnoreCase(targetCommand))
      super.visitStatement(stmt, position)(ops + (position -> manipulate _))
    else
      super.visitStatement(stmt, position)
}
  }

object WhiteSpaceTracker {
  case class Context(lastEnd: Option[Int],
    astWsMap: Map[Seq[AstPath], String],
    lastFile: Option[String] = None,
    initialWs: String = "",
    finalWs: String = "")
}

class WhiteSpaceTracker(sources: Map[String, String]) extends PositionalAstFolder[WhiteSpaceTracker.Context] {
  import AstPath._
  import WhiteSpaceTracker._

  def sourceFromLast(c: Context, astNode: AstNode, getTargetPoint: AstNode => Int): String =
    c.lastEnd.map(e => sources(astNode.file).slice(e, getTargetPoint(astNode)))
      .getOrElse("")
      .replaceAllLiterally("[", "")
      .replaceAllLiterally("]", "")

  override def visitProcedureDefinition(proc: ProcedureDefinition)(c: Context): Context = {
    val initialWs = sourceFromLast(c, proc, _.start)
    val functionHeaderEnd = proc.procedure.argTokens.lastOption.getOrElse(proc.procedure.nameToken).end
    val functionHeader = sources(proc.file).slice(proc.start, functionHeaderEnd)
    val c1 = c.copy(initialWs = initialWs + functionHeader, lastEnd = Some(functionHeaderEnd))
    val c2 = super.visitProcedureDefinition(proc)(c1)
    // +3 to capture END token
    c2.copy(finalWs = sourceFromLast(c2, proc, _.end + 3), lastEnd = Some(proc.end + 3))
  }

  override def visitCommandBlock(block: CommandBlock, position: Seq[AstPath])(implicit a: Context): Context = {
    super.visitCommandBlock(block, position)
  }

  override def visitReporterApp(app: ReporterApp, position: Seq[AstPath])(implicit c: Context): Context = {
    if (app.reporter.syntax.isInfix) {
      val c1 = visitExpression(app.args.head, position, 0)(c)
      val c2 = c1.copy(lastEnd = Some(app.reporter.token.end),
        astWsMap = c1.astWsMap + (position -> sourceFromLast(c1, app, _ => app.reporter.token.start)))
      app.args.zipWithIndex.tail.foldLeft(c2) {
        case (ctx, (arg, i)) => visitExpression(arg, position, i)(ctx)
      }
    } else {
      val c1 = c.copy(
        lastEnd = Some(app.reporter.token.end),
        astWsMap = c.astWsMap + (position -> sourceFromLast(c, app, _.start)))
      super.visitReporterApp(app, position)(c1)
    }
  }

  override def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit c: Context): Context = {
    val newCtxt = c.copy(
      lastEnd = Some(stmt.command.token.end),
      astWsMap = c.astWsMap + (position -> sourceFromLast(c, stmt, _.start)))
    super.visitStatement(stmt, position)(newCtxt)
  }
}

object Formatter {

  type Operation = (Formatter, AstNode, Seq[AstPath], Context) => Context
  case class Context(
    text: String,
    operations: Map[Seq[AstPath], Operation],
    instructionToString: Instruction => String = instructionString _,
    wsMap: Map[Seq[AstPath], String] = Map())

  def instructionString(i: Instruction): String =
    i match {
      case _const(value) if value.isInstanceOf[LogoList] => Dump.logoObject(value, true, false)
      case r: _const        => r.token.text
      case r: _reportertask => ""
      case r: _commandtask  => ""
      case r                => r.token.text
    }

  def deletedInstructionToString(i: Instruction): String = ""
}

class Formatter
  extends PositionalAstFolder[Formatter.Context] {

  import Formatter.{ Context, deletedInstructionToString }

  override def visitCommandBlock(block: CommandBlock, position: Seq[AstPath])(implicit c: Context): Context = {
    visitBlock(block, position, c1 => super.visitCommandBlock(block, position)(c1))
  }

  override def visitReporterBlock(block: ReporterBlock, position: Seq[AstPath])(implicit c: Context): Context = {
    visitBlock(block, position, c1 => super.visitReporterBlock(block, position)(c1))
  }

  private def visitBlock(block: AstNode, position: Seq[AstPath], visit: Context => Context)(implicit c: Context): Context = {
    c.operations.get(position)
      .map(op => op(this, block, position, c))
      .getOrElse {
        val r = visit(c.copy(c.text + " [ "))
        r.copy(text = r.text + " ] ")
      }
  }

  override def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit c: Context): Context = {
    c.operations.get(position)
      .map(op => op(this, stmt, position, c))
      .getOrElse {
        val ws = leadingWhitespace(position)
        val newContext = c.copy(text = c.text + ws + c.instructionToString(stmt.command))
        super.visitStatement(stmt, position)(newContext)
          .copy(instructionToString = c.instructionToString)
      }
  }

  override def visitReporterApp(app: ReporterApp, position: Seq[AstPath])(implicit c: Context): Context = {
    import org.nlogo.core.prim._reportertask

    c.operations.get(position)
      .map(op => op(this, app, position, c))
      .getOrElse {
        val ws = leadingWhitespace(position)
        (app.reporter.syntax.isInfix, app.reporter) match {
          case (true, i) =>
            val c2 = visitExpression(app.args.head, position, 0)(c)
            app.args.zipWithIndex.tail.foldLeft(c2.copy(text = c2.text + ws + c.instructionToString(i))) {
              case (ctx, (arg, i)) => visitExpression(arg, position, i)(ctx)
            }
          case (false, _: _reportertask) =>
            val c2 = super.visitReporterApp(app, position)(Context("", c.operations, wsMap = c.wsMap))
            c.copy(text = c.text + " [ " + c2.text + " ] ")
          case (false, reporter) =>
            super.visitReporterApp(app, position)(c.copy(text = c.text + ws + c.instructionToString(reporter)))
              .copy(instructionToString = c.instructionToString)
        }
      }
  }

  private def leadingWhitespace(path: Seq[AstPath])(implicit c: Context): String =
    c.wsMap.getOrElse(path, "")
}

trait AstPath
object AstPath {
  case class Proc(name: String) extends AstPath
  case class Stmt(pos: Int) extends AstPath
  trait Exp extends AstPath {
    def pos: Int
  }
  case class RepArg(pos: Int) extends Exp
  case class CmdBlk(pos: Int) extends Exp
  case class RepBlk(pos: Int) extends Exp

  def Expression(exp: Expression, index: Int): AstPath = {
    exp match {
      case app: ReporterApp => RepArg(index)
      case cb: CommandBlock => CmdBlk(index)
      case rb: ReporterBlock => RepBlk(index)
    }
  }
}

trait PositionalAstFolder[A] {
  import AstPath._
  def visitProcedureDefinition(proc: ProcedureDefinition)(a: A): A = {
    visitStatements(proc.statements, Seq(Proc(proc.procedure.name.toUpperCase)))(a)
  }
  def visitCommandBlock(block: CommandBlock, position: Seq[AstPath])(implicit a: A): A = {
    visitStatements(block.statements, position)
  }
  def visitExpression(exp: Expression, position: Seq[AstPath], index: Int)(implicit a: A): A = {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app, position :+ RepArg(index))
      case cb: CommandBlock =>
        visitCommandBlock(cb, position :+ CmdBlk(index))
      case rb: ReporterBlock =>
        visitReporterBlock(rb, position :+ RepBlk(index))
    }
  }

  def visitReporterApp(app: ReporterApp, position: Seq[AstPath])(implicit a: A): A = {
    app.args.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitExpression(arg, position, i)(acc)
    }
  }

  def visitReporterBlock(block: ReporterBlock, position: Seq[AstPath])(implicit a: A): A = {
    visitReporterApp(block.app, position)
  }

  def visitStatement(stmt: Statement, position: Seq[AstPath])(implicit a: A): A = {
    stmt.args.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitExpression(arg, position, i)(acc)
    }
  }

  def visitStatements(statements: Statements, position: Seq[AstPath])(implicit a: A): A = {
    statements.stmts.zipWithIndex.foldLeft(a) {
      case (acc, (arg, i)) => visitStatement(arg, position :+ Stmt(i))(acc)
    }
  }
}
