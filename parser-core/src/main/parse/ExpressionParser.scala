// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ prim, FrontEndProcedure, I18N, SourceLocatable, SourceLocation, Syntax, Token, TokenType },
    prim.Lambda,
    Syntax.compatible

import SymbolType.LocalVariable

import scala.annotation.tailrec
import collection.mutable.Buffer

import scala.util.{ Failure, Success, Try }

/**
 * Parses procedure bodies.
 */

object ExpressionParser {

  /**
   * one less than the lowest valid operator precedence. See Syntax.
   */
  private val MinPrecedence = -1

  case class ParseContext(variadic: Boolean, scope: SymbolTable) {
    def withVariadic(v: Boolean) = copy(variadic = v)
    def withScope(symbols: SymbolTable) = copy(scope = symbols)
  }

  case class ArgumentParseContext(instruction: core.Instruction, location: SourceLocation) {
    val syntax = instruction.syntax
    val displayName = instruction.displayName
    val sourceLocation = location
    def missingInput(i: Int): String = ExpressionParser.missingInput(syntax, displayName, i)
    def parseArgumentContext(i: Int): ExpressionParseContext =
      ExpressionParseContext(syntax.right(i), syntax.precedence)
    def variadic = syntax.isVariadic
  }

  case class ExpressionParseContext(goalType: Int, precedence: Int)

  private def isEnd(g: SyntaxGroup, end: TokenType): Boolean = {
    g match {
      case Atom(a) => a.tpe == end
      case _ => false
    }
  }

  type RemainingParseResult[A] = ParseResult[(A, Seq[SyntaxGroup])]

  // prototypical operation
  // (Seq[SyntaxGroup], A) => Either[Failure, (Seq[SyntaxGroup], B)]
  //
  // We could think of decomposing this into a state (Seq[SyntaxGroup]) and a validation (Failure).
  object ParseResult {
    def apply[A](a: A): ParseResult[A] = SuccessfulParse(a)
    def fromTry[A](t: Try[A]) =
      t match {
        case Success(x) => SuccessfulParse(x)
        case Failure(e: core.CompilerException) => FailedParse(ParseFailure(e.getMessage, e.start, e.end, e.filename))
        case Failure(e) => throw e
      }
  }

  sealed trait ParseResult[+A] {
    def flatMap[B](f: A => ParseResult[B]): ParseResult[B]
    def map[B](f: A => B): ParseResult[B]
    def get: A
    def exists(f: A => Boolean): Boolean
    def recoverWith[B >: A](f: PartialFunction[ParseFailure, ParseResult[B]]): ParseResult[B]
  }

  case class ParseFailure(message: String, start: Int, end: Int, filename: String) {
    def toException = new core.CompilerException(message, start, end, filename)
  }

  case class FailedParse(failure: ParseFailure) extends ParseResult[Nothing] {
    def flatMap[A](f: Nothing => ParseResult[A]): ParseResult[A] = this
    def map[A](f: Nothing => A): ParseResult[A] = this
    def get = throw failure.toException
    def exists(f: Nothing => Boolean): Boolean = false
    def recoverWith[A](f: PartialFunction[ParseFailure, ParseResult[A]]): ParseResult[A] =
      if (f.isDefinedAt(failure)) f(failure)
      else this
  }

  private class MissingPrefixFailure(val token: Token) extends
    ParseFailure("Missing prefix", token.start, token.end, token.filename)

  private class UnexpectedTokenFailure(val token: Token) extends
    ParseFailure("Unexpected token", token.start, token.end, token.filename)

  private object TypeMismatch {
    def unapply(t: TypeMismatch): Option[(Int, Int)] = Some((t.expectedType, t.actualType))
  }

  private class TypeMismatch(val arg: core.Expression, message: String, val expectedType: Int, val actualType: Int) extends
    ParseFailure(message, arg.start, arg.end, arg.filename)

  case class SuccessfulParse[A](val parsed: A) extends ParseResult[A] {
    def flatMap[B](f: A => ParseResult[B]): ParseResult[B] = f(parsed)
    def map[B](f: A => B): ParseResult[B] = new SuccessfulParse(f(parsed))
    def get = parsed
    def exists(f: A => Boolean): Boolean = f(parsed)
    def recoverWith[B >: A](f: PartialFunction[ParseFailure, ParseResult[B]]): ParseResult[A] =
      this
  }

  def fail(message: String, locatable: SourceLocatable): FailedParse =
    fail(message, locatable.sourceLocation)
  def fail(message: String, location: SourceLocation): FailedParse =
    fail(message, location.start, location.end, location.filename)
  def fail(message: String, start: Int, end: Int, filename: String): FailedParse =
    FailedParse(ParseFailure(message, start, end, filename))


  implicit class RichRemainingParseResult[A](p: RemainingParseResult[A]) {
    def mapGroups(f: Seq[SyntaxGroup] => Seq[SyntaxGroup]): RemainingParseResult[A] =
      p.map(((r: A, gs: Seq[SyntaxGroup]) => (r, f(gs))).tupled)
    def mapResult[B](f: A => B): RemainingParseResult[B] =
      p.map(((r: A, gs: Seq[SyntaxGroup]) => (f(r), gs)).tupled)
    def setRest(gs: Seq[SyntaxGroup]): RemainingParseResult[A] =
      mapGroups(_ => gs)
    def setResult[B](b: B): RemainingParseResult[B] =
      mapResult(_ => b)
  }

  // Should Partials include scope and variadicity?????

  sealed trait Partial {
    // Primacy gives the order in which partials ought to be reduced.
    // Where possible, we collapse a command to statement before parsing the next command
    def primacy: Int
    def needsArguments = false
    def neededArgument: Int = 0
  }

  trait ApplicationPartial extends Partial {
    def syntax: core.Syntax
    def args: Seq[core.Expression]
    def instruction: core.Instruction
    def precedence = syntax.precedence
    override def needsArguments = syntax.totalCount > args.length
    override def neededArgument: Int =
      if (needsArguments) syntax.allArgs(args.length)
      else if (isVariadic) syntax.right.last
      else 0
    // This imposes a restriction on the compiler - concise primitives cannot be in a variadic position
    def needsSymbolicArgument: Boolean =
      (neededArgument == Syntax.CommandType) ||
        (neededArgument == Syntax.ReporterType) ||
        (neededArgument == (Syntax.ReporterType | Syntax.CommandType)) ||
        (neededArgument == Syntax.SymbolType) ||
        (neededArgument == Syntax.ReferenceType)
    def parseContext = ArgumentParseContext(instruction, instruction.token.sourceLocation)
    def withArgument(arg: core.Expression): ApplicationPartial
    def isVariadic = syntax.isVariadic
  }

  trait ArgumentPartial extends Partial {
    def expression: core.Expression
    def providedArgument: Int
  }

  object PartialInstruction {
    def unapply(p: Partial): Option[(core.Instruction, core.Token)] =
      p match {
        case PartialReporter(rep, tok) => Some((rep, tok))
        case PartialCommand(cmd, tok) => Some((cmd, tok))
        case PartialInfixReporter(rep, tok) => Some((rep, tok))
        case _ => None
      }
  }

  case class PartialStatements(stmts: core.Statements) extends Partial {
    val primacy = 7
  }
  case class PartialStatement(stmt: core.Statement) extends Partial {
    val primacy = 6
  }
  case class PartialCommandAndArgs(cmd: core.Command, tok: Token, args: Seq[core.Expression]) extends Partial with ApplicationPartial {
    val primacy = 5
    def syntax = cmd.syntax
    def withArgument(arg: core.Expression): ApplicationPartial =
      copy(args = args :+ arg)
    def instruction = cmd
  }
  case class PartialReporterAndArgs(rep: core.Reporter, tok: Token, args: Seq[core.Expression]) extends Partial with ApplicationPartial {
    val primacy = 4
    def syntax = rep.syntax
    def withArgument(arg: core.Expression): ApplicationPartial =
      copy(args = args :+ arg)
    def instruction = rep
  }
  // this one is particularly odd, a raw command can *sometimes* end up being an Argument,
  // but sometimes ends up being the start of a statement
  case class PartialCommand(cmd: core.Command, tok: Token) extends Partial {
    val primacy = 3
  }
  case class PartialReporterBlock(block: core.ReporterBlock) extends ArgumentPartial {
    val primacy = 2
    def expression = block
    def providedArgument: Int = Syntax.ReporterBlockType
  }
  case class PartialCommandBlock(block: core.CommandBlock) extends ArgumentPartial {
    val primacy = 2
    def expression = block
    def providedArgument: Int = Syntax.CommandBlockType
  }
  case class PartialReporterApp(app: core.ReporterApp) extends ArgumentPartial {
    val primacy = 2
    def expression = app
    def providedArgument: Int = app.reporter.syntax.ret
  }
  case class PartialDelayedBlock(db: DelayedBlock) extends Partial {
    val primacy = 2
  }
  case class PartialInfixReporter(rep: core.Reporter, tok: Token) extends Partial {
    val primacy = 1
  }
  case class PartialReporter(rep: core.Reporter, tok: Token) extends Partial {
    val primacy = 1
  }
  case class PartialError(failure: FailedParse) extends Partial {
    val primacy = -1
  }

  case class ParsingContext(precedence: Int, scope: SymbolTable, variadic: Boolean)

  def reduce(stack: List[Partial], ctx: ParsingContext): (List[Partial], ParsingContext) = {
    // I hope both of these two lines are only temporary
    import scala.language.implicitConversions
    implicit def partialListToTuple(l: List[Partial]): (List[Partial], ParsingContext) = (l, ctx)

    import ctx.scope

    // println("context precedence: " + ctx.precedence)
    // println(stack.reverse.mkString("//"))

    stack match {
      // Stmts -> Stmt | Stmts Stmt
      case PartialStatement(stmt) :: Nil =>
        PartialStatements(new core.Statements(stmt.filename, Seq(stmt))) :: Nil
      // Stmts -> Stmt | Stmts Stmt
      case PartialStatement(s) :: PartialStatements(stmts) :: rest =>
        // end *may* not be correct here when there is a block
        PartialStatements(stmts.copy(stmts = stmts.stmts :+ s)) :: rest
      // Error(ExpectedCommand) -> Stmt id
      case PartialReporter(rep: core.prim._unknownidentifier, tok) :: PartialStatement(_) :: rest =>
        List(PartialError(fail(ExpectedCommand, tok)))
      case PartialInstruction(ins, tok) :: (ap: ApplicationPartial) :: rest if ap.neededArgument == Syntax.SymbolType =>
        (ap.withArgument(processSymbol(tok)) :: rest, ctx.copy(precedence = ap.precedence))
      // Arg -> ConciseReporter | ConciseCommand | RepApp | ReporterBlock | CommandBlock
      case PartialInstruction(rep: core.Reporter, tok) :: (ap: ApplicationPartial) :: rest if ap.neededArgument == Syntax.ReporterType =>
        (processReporter(rep, tok, Seq(), ap.neededArgument, scope) :: ap :: rest, ctx.copy(precedence = ap.precedence))
      // Arg -> ConciseCommand | ConciseReporter | RepApp | ReporterBlock | CommandBlock
      case PartialCommand(cmd, tok) :: (ap: ApplicationPartial) :: rest                    if ap.needsSymbolicArgument =>
        (cmdToReporterApp(cmd, tok, ap.neededArgument, ap.parseContext, scope) :: ap :: rest, ctx.copy(precedence = ap.precedence))
      // Arg -> CommandBlock | ReporterBlock | RepApp | ConciseCommand | ConciseReporter
      case (arg: ArgumentPartial) :: (ap: ApplicationPartial) :: rest =>
        resolveType(ap.neededArgument, arg.expression, ap.instruction.displayName, ctx.scope) match {
          case f: FailedParse       => List(PartialError(f))
          case SuccessfulParse(exp) => ap.withArgument(exp) :: rest
        }
      case PartialReporterAndArgs(rep, tok, args) :: (ap: ApplicationPartial) :: rest if ap.needsArguments || (ap.isVariadic && (ctx.variadic || ap.args.length < ap.syntax.rightDefault)) =>
        (processReporter(rep, tok, args, ap.neededArgument, scope) :: ap :: rest, ctx.copy(precedence = ap.precedence))
      case PartialReporter(rep, tok) :: rest =>
        PartialReporterAndArgs(rep, tok, Seq()) :: rest
      case (ra: PartialReporterApp) :: PartialCommand(cmd, tok) :: rest =>
        ra :: PartialCommandAndArgs(cmd, tok, Seq()) :: rest
      case PartialReporterApp(app) :: rest =>
        List(PartialError(fail(ExpectedCommand, app.reporter.token)))
      case PartialCommandAndArgs(cmd, tok, args) :: rest =>
        val (p, newScope) = processStatement(cmd, tok, args, scope)
        (p :: rest, ctx.copy(scope = newScope))
      case PartialInfixReporter(iRep, iTok) :: (arg: ArgumentPartial) :: rest =>
        (PartialReporterAndArgs(iRep, iTok, Seq(arg.expression)) :: rest, ctx.copy(precedence = iRep.syntax.precedence))
      case PartialReporterAndArgs(_, tok, _) :: (_: PartialStatement | _: PartialStatements) :: rest =>
        List(PartialError(fail(ExpectedCommand, tok)))
      case (ir@PartialInfixReporter(iRep, iTok)) :: PartialDelayedBlock(block) :: rest =>
        processDelayedBlock(block, iRep.syntax.left, scope) match {
          case p: PartialError => p :: rest
          case other => ir :: other :: rest
        }
      case PartialInfixReporter(iRep: core.prim._minus, iTok) :: rest if ctx.variadic =>
        val rep = new core.prim._unaryminus()
        iTok.refine(rep)
        PartialReporter(rep, iTok) :: rest
      case PartialInfixReporter(iRep, iTok) :: rest =>
        List(PartialError(fail(ArgumentParseContext(iRep, iTok.sourceLocation).missingInput(0), iTok.sourceLocation)))
      case PartialCommand(cmd, tok) :: rest =>
        PartialCommandAndArgs(cmd, tok, Seq()) :: rest
      case PartialDelayedBlock(db) :: (pc: ApplicationPartial) :: rest if pc.needsArguments =>
        processDelayedBlock(db, pc.neededArgument, scope) :: pc :: rest
      case (pr@PartialReporterAndArgs(rep, tok, args)) :: Nil if ! pr.needsArguments => // this case comes up when parsing reporter lambda (and probably blocks)
        (processReporter(rep, tok, args, Syntax.WildcardType, scope) :: Nil, ctx.copy(precedence = Syntax.CommandPrecedence))
      case PartialDelayedBlock(db) :: Nil => // this case comes up when parsing reporter lambda (and possibly reporter blocks)
        processDelayedBlock(db, Syntax.WildcardType, scope) :: Nil
      case _ => List(PartialError(fail("unknown parse for: " + stack.reverse.mkString(" // "), SourceLocation(0, 0, ""))))
    }
  }

  // Things remaining as of 5/11/17:
  // * handling `of` and `with`
  // * Coherency / simplicity
  def canProvide(g: SyntaxGroup) = {
    g match {
      case b: BracketGroup =>
        Syntax.CommandType | Syntax.CommandBlockType |
        Syntax.ReporterBlockType | Syntax.ReporterType |
        Syntax.ListType | Syntax.CodeBlockType
      case p: ParenGroup => Syntax.WildcardType
      case a: Atom       =>
        a.token.tpe match {
          case TokenType.Reporter  => Syntax.WildcardType
          case TokenType.Literal   => Syntax.ReadableType
          case TokenType.Ident     => Syntax.WildcardType
          case TokenType.Extension => Syntax.WildcardType
          case _ => Syntax.VoidType
        }
    }
  }

  def canBeSymbolic(g: SyntaxGroup) = {
    g match {
      case a: Atom =>
        a.token.tpe match {
          case TokenType.Reporter | TokenType.Command | TokenType.Ident => true
          case _ => false
        }
      case _ => false
    }
  }

  def isCommand(g: SyntaxGroup): Boolean = {
    g match {
      case Atom(Token(_, TokenType.Command, _)) => true
      case p: ParenGroup => isCommand(p.innerGroups.head)
      case _ => false
    }
  }

  def shouldShift(p: Partial, g: SyntaxGroup, c: ParsingContext): Boolean = {
    val stackPrimacy = p.primacy
    (p, g) match {
      case (_, pg: ParenGroup) =>
        pg.innerGroups.headOption.map(headGroup => shouldShift(p, headGroup, c)).getOrElse(stackPrimacy > 3)
      case (ap: ApplicationPartial, g) if ap.needsSymbolicArgument && canBeSymbolic(g) =>
        true
      case (ap: ApplicationPartial, _) if ! isCommand(g) =>
        ap.needsArguments || (ap.isVariadic && (c.variadic || ap.args.length < ap.syntax.rightDefault)) && compatible(ap.neededArgument, canProvide(g))
      case (db: PartialDelayedBlock, bg: BracketGroup) => false
      case (_, bg: BracketGroup) =>
        stackPrimacy > 3
      case (_, Atom(token@Token(_, TokenType.Command, cmd: core.Command))) =>
        stackPrimacy > 6
      case (p, Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter))) if rep.syntax.isInfix =>
        (c.precedence < rep.syntax.precedence) && ((p.needsArguments && compatible(p.neededArgument, rep.syntax.ret)) || stackPrimacy > 1)
      case (p, Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter))) =>
        p.needsArguments || stackPrimacy > 3
      case (p, Atom(token@Token(_, TokenType.Literal, _))) =>
        p.needsArguments || stackPrimacy > 3
      case o => throw new NotImplementedError(s"shift precedence undefined for $o")
    }
  }

  def shift(p: Option[Partial], g: SyntaxGroup, ctx: ParsingContext): Partial = {
    g match {
      case Atom(token@Token(_, TokenType.Command, cmd: core.Command)) =>
        PartialCommand(cmd, token)
      case Atom(token@Token(_, TokenType.Literal, literalVal)) =>
        val coreReporter = new core.prim._const(token.value)
        token.refine(coreReporter)
        PartialReporter(coreReporter, token)
      case Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter)) if rep.syntax.isInfix =>
        PartialInfixReporter(rep, token)
      case Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter)) =>
        PartialReporter(rep, token)
      case bg: BracketGroup =>
        PartialDelayedBlock(DelayedBlock(bg, ctx.scope))
      case pg@ParenGroup(inner, start, end) =>
        val error = p match {
          case Some(ap: ApplicationPartial) => fail(ExpectedReporter, pg.location)
          case _ => fail(ExpectedCommand, pg.location)
        }
        // failing with ExpectedCommand may not be appropriate here...
        val intermediateResult = runRec(Nil, inner, ctx.copy(variadic = true), p => p.isInstanceOf[PartialReporterApp] || p.isInstanceOf[PartialStatements], error)
        intermediateResult match {
          case SuccessfulParse((PartialStatements(stmts), Seq())) =>
            if (stmts.stmts.length == 1) PartialStatement(stmts.stmts.head)
            else PartialError(fail(ExpectedCloseParen, stmts.stmts(1).command.token))
          case SuccessfulParse((p: PartialReporterApp, Seq())) => p
          case SuccessfulParse((p, Seq(g, _*))) => PartialError(fail(ExpectedCommand, g.location))
          case f: FailedParse => PartialError(f)
        }
      case other => throw new NotImplementedError(s"shift undefined for $other")
    }
  }

  def runRec(stack: List[Partial], groups: Seq[SyntaxGroup], ctx: ParsingContext, finished: Partial => Boolean, emptyDefault: ParseResult[Partial]): RemainingParseResult[Partial] = {
    stack.headOption match {
      case Some(PartialError(failure)) => failure
      case _ =>
        if (groups.isEmpty && stack.length == 1 && stack.headOption.exists(finished))
          SuccessfulParse((stack.head, Seq()))
        else if (groups.isEmpty && stack.isEmpty)
          emptyDefault.map(p => (p, groups))
        else if (groups.nonEmpty && stack.headOption.forall(p => shouldShift(p, groups.head, ctx)))
          runRec(shift(stack.headOption, groups.head, ctx) :: stack, groups.tail, ctx, finished, emptyDefault)
        else {
          val (newStack, newCtx) = reduce(stack, ctx)
          runRec(newStack, groups, newCtx, finished, emptyDefault)
        }
    }
  }

  def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {

    //.init to avoid Eof awkwardness
    val groupedTokens = groupSyntax(tokens.buffered).get
    runRec(Nil, groupedTokens.init, ParsingContext(Syntax.CommandPrecedence, scope, false), _.isInstanceOf[PartialStatements],
      SuccessfulParse(PartialStatements(new core.Statements(procedureDeclaration.filename, Seq())))).get match {
      case (PartialError(f), _)         => throw f.failure.toException
      case (PartialStatements(stmts), _) =>
        val tokenEnd = groupedTokens.lastOption.map(_.end.start).getOrElse(Int.MaxValue)
        val end = if (tokenEnd < Int.MaxValue) tokenEnd else stmts.end
        new core.ProcedureDefinition(procedureDeclaration, stmts, end)
      case other                        => throw new Error("bad parse?! " + other)
    }
  }

  def processStatement(cmd: core.Command, tok: Token, args: Seq[core.Expression], scope: SymbolTable): (Partial, SymbolTable) = {
    // this is the work currently done by LetScoper...
    val parseLetAndScope =
      cmd match {
        case l @ core.prim._let(Some(let)) =>
          SuccessfulParse((l, scope.addSymbol(let.name.toUpperCase, LocalVariable(let))))
        case l @ core.prim._let(None) =>
          args.headOption match {
            case Some(core.ReporterApp(name: core.prim._symbol, _, _)) =>
              val properName = name.token.text.toUpperCase
              scope.get(properName)
                .map(tpe => fail("There is already a " + SymbolType.typeName(tpe) + " called " + properName, name.token))
                .getOrElse {
                  val newLet = core.Let(properName)
                  SuccessfulParse((l.copy(let = newLet), scope.addSymbol(properName, LocalVariable(newLet))))
                }
            case Some(other) => fail("expected letname, found: " + other, other)
            case None => SuccessfulParse((l, scope)) // this failure is handled below
          }
        case other => SuccessfulParse((other, scope))
      }
    val allArgs =
      if (cmd.syntax.takesOptionalCommandBlock && args.length == cmd.syntax.right.length - 1) {
        args :+ syntheticCommandBlock(args, cmd.token.sourceLocation)
      } else
        args

    parseLetAndScope match {
      case f: FailedParse => (PartialError(f), scope)
      case SuccessfulParse((newCmd, newScope)) =>
        resolveTypes(allArgs, ArgumentParseContext(newCmd, tok.sourceLocation), scope) match {
          case f: FailedParse => (PartialError(f), scope)
          case SuccessfulParse(typedArgs) =>
            val loc = SourceLocation(tok.start, args.lastOption.map(_.sourceLocation.end).getOrElse(tok.end), tok.filename)
            (PartialStatement(new core.Statement(newCmd, typedArgs, loc)), newScope)
        }
    }
  }

  def processSymbol(tok: Token): core.ReporterApp = {
    val symbol = new core.prim._symbol()
    tok.refine(symbol)
    new core.ReporterApp(symbol, tok.sourceLocation)
  }

  def processReporter(rep: core.Reporter, tok: Token, args: Seq[core.Expression], goalType: Int, scope: SymbolTable): Partial = {
    val newRepApp: ParseResult[core.ReporterApp] =
      if (rep.isInstanceOf[core.prim._const])
        SuccessfulParse(new core.ReporterApp(rep, tok.sourceLocation))
      else if (compatible(goalType, Syntax.SymbolType))
        SuccessfulParse(processSymbol(tok))
      else if (goalType == Syntax.ReporterType) {
        val rApp = new core.ReporterApp(rep, tok.sourceLocation)
        SuccessfulParse(expandConciseReporterLambda(rApp, rep, scope))
      } else {
        rep match {
          case s: core.prim._symbol =>
            scope.get(s.token.text.toUpperCase)
              .map(tpe => fail(SymbolType.alreadyDefinedMessage(tpe, tok), tok))
              .getOrElse(fail(I18N.errors.getN("compiler.LetVariable.notDefined", tok.text.toUpperCase), tok))
          case u: core.prim._unknownidentifier =>
            val newInstruction: Option[core.Reporter] =
              scope.get(tok.text.toUpperCase).collect {
                case LocalVariable(let) =>
                  val newLetVariable = core.prim._letvariable(let)
                  newLetVariable.token = tok
                  newLetVariable
              }
            newInstruction
              .map(i => new core.ReporterApp(i, tok.sourceLocation))
              .map(SuccessfulParse.apply _)
              .getOrElse(fail(I18N.errors.getN("compiler.LetVariable.notDefined", tok.text.toUpperCase), tok))
          case other =>
            SuccessfulParse(new core.ReporterApp(other, args, tok.sourceLocation))
        }
      }


    // NOTE: We removed argument typing, we may need to re-add it
    newRepApp match {
      case f: FailedParse => PartialError(f)
      case SuccessfulParse(repApp) =>
        resolveTypes(repApp.args, ArgumentParseContext(repApp.reporter, repApp.sourceLocation), scope) match {
          case f: FailedParse => PartialError(f)
          case SuccessfulParse(typedArgs) =>
            val start = args.headOption
              .map(_.sourceLocation.start min tok.start)
              .getOrElse(tok.start)
            val end = args.lastOption.map(_.sourceLocation.end).getOrElse(tok.end)
            val loc = SourceLocation(start, end, tok.filename)
            PartialReporterApp(repApp.copy(args = typedArgs, location = loc))
        }
    }
  }

  def cmdToReporterApp(cmd: core.Command, tok: Token, goalType: Int, parseContext: ArgumentParseContext, scope: SymbolTable): Partial = {
    if (! cmd.syntax.canBeConcise) // this error may need to be one of two different things, depending on parent context
      if (parseContext.instruction.isInstanceOf[core.Reporter])
        PartialError(fail(ExpectedReporter, tok))
      else
        PartialError(fail(parseContext.missingInput(0), parseContext.location))
    else {
      val (varNames, varApps) = syntheticVariables(cmd.syntax.totalDefault, tok, scope)
      val stmtArgs =
        if (cmd.syntax.takesOptionalCommandBlock)
          // synthesize an empty block so that later phases of compilation will be dealing with a
          // consistent number of arguments - ST 3/4/08
          varApps :+ new core.CommandBlock(new core.Statements(tok.filename), tok.sourceLocation, synthetic = true)
        else varApps

      val lambda = new core.prim._commandlambda(Lambda.ConciseArguments(varNames))
      lambda.token = tok

      val stmt = new core.Statement(cmd, stmtArgs, tok.sourceLocation)

      val commandBlock = commandBlockWithStatements(tok.sourceLocation, Seq(stmt), synthetic = true)

      PartialReporterApp(new core.ReporterApp(lambda, Seq(commandBlock), tok.sourceLocation))
    }
  }

  def processDelayedBlock(block: DelayedBlock, goalType: Int, scope: SymbolTable): Partial = {
    if (compatible(goalType, Syntax.CodeBlockType))
      processCodeBlock(block)
    else if (block.isArrowLambda && ! block.isCommand)
      processReporterLambda(block.asInstanceOf[ArrowLambdaBlock], scope) // TODO: switch this to a `case`?
    else if (block.isArrowLambda)
      processCommandLambda(block.asInstanceOf[ArrowLambdaBlock], scope) // TODO: switch this to a `case`?
    else if (compatible(goalType, Syntax.ReporterBlockType))
      processReporterBlock(block, scope)
    else if (compatible(goalType, Syntax.CommandBlockType))
      processCommandBlock(block, scope)
    else if (compatible(goalType, Syntax.ListType))
      processLiteralList(block)
    else if (compatible(goalType, Syntax.ReporterType) && !block.isCommand)
      processReporterLambda(block.toLambda, scope)
    else if (compatible(goalType, Syntax.CommandType) && block.isCommand)
      processCommandLambda(block.toLambda, scope)
    else
      PartialError(fail(s"Expected ${core.TypeNames.aName(goalType)} here, rather than a list or block.", block))
  }

  def processReporterBlock(block: DelayedBlock, scope: SymbolTable): Partial = {
    runRec(Nil, block.bodyGroups, ParsingContext(Syntax.CommandPrecedence, block.internalScope, false),
      _.isInstanceOf[PartialReporterApp],
      fail(ExpectedReporter, block.group.location)).flatMap {
      case (PartialReporterApp(app), remainingGroups) =>
        resolveType(Syntax.WildcardType, app, null, scope).map[Partial] {
          case (expr: core.ReporterApp) =>
            PartialReporterBlock(new core.ReporterBlock(expr, block.group.location))
          case (other: core.Expression) =>
            PartialError(fail(ExpectedReporter, other))
        }
      case (other, remainingGroups) => fail(ExpectedCommand, remainingGroups.head.start)
    } match {
      case f: FailedParse => PartialError(f)
      case SuccessfulParse(partial) => partial
    }
  }

  def processReporterLambda(block: ArrowLambdaBlock, scope: SymbolTable): Partial = {
    runRec(Nil, block.bodyGroups, ParsingContext(Syntax.CommandPrecedence, block.internalScope, false),
      _.isInstanceOf[PartialReporterApp],
      fail(ExpectedReporter, block.group.location)).flatMap {
      case (PartialReporterApp(app), remainingGroups) =>
        resolveType(Syntax.WildcardType, app, null, scope).map[Partial] {
          case (expr: core.ReporterApp) =>
            val lambda = new core.prim._reporterlambda(Lambda.ConciseArguments(block.argNames))
            lambda.token = block.openBracket
            PartialReporterApp(new core.ReporterApp(lambda, Seq(expr), block.group.location))
          case (other: core.Expression) =>
            PartialError(fail(ExpectedCommand, other))
        }
      case (other, remainingGroups) => fail(ExpectedCommand, remainingGroups.head.start)
    } match {
      case f: FailedParse => PartialError(f)
      case SuccessfulParse(partial) => partial
    }
  }

  def processCommandBlock(block: DelayedBlock, scope: SymbolTable): Partial = {
    if (block.bodyGroups.isEmpty) {
      val file = block.group.start.filename
      PartialCommandBlock(new core.CommandBlock(
        new core.Statements(file),
        SourceLocation(block.group.start.start, block.group.end.end, file)))
    } else
      runRec(Nil, block.bodyGroups, ParsingContext(Syntax.CommandPrecedence, block.internalScope, false),
        _.isInstanceOf[PartialStatements],
        SuccessfulParse(PartialStatements(new core.Statements(block.group.location.filename, Seq())))).flatMap {
        case (PartialStatements(stmts), remainingGroups) if remainingGroups.isEmpty =>
          SuccessfulParse(PartialCommandBlock(commandBlockWithStatements(block.group.location, stmts.stmts)))
        case (_, remainingGroups) => fail(ExpectedCommand, remainingGroups.head.start)
      } match {
        case f: FailedParse => PartialError(f)
        case SuccessfulParse(partial) => partial
      }
  }

  private def processCodeBlock(block: DelayedBlock): Partial = {
    val tokens = block match {
      case alb: ArrowLambdaBlock => alb.allTokens
      case adl: AmbiguousDelayedBlock => adl.tokens
    }

    val tmp = new core.prim._constcodeblock(tokens.tail.dropRight(2))
    tmp.token = tokens.head
    PartialReporterApp(new core.ReporterApp(tmp, SourceLocation(tokens.head.start, block.end, tokens.head.filename)))
  }

  def processCommandLambda(block: ArrowLambdaBlock, scope: SymbolTable): Partial = {
    runRec(Nil, block.bodyGroups, ParsingContext(Syntax.CommandPrecedence, block.internalScope, false),
      _.isInstanceOf[PartialStatements],
      SuccessfulParse(PartialStatements(new core.Statements(block.group.location.filename, Seq())))).flatMap {
      case (PartialStatements(stmts), remainingGroups) if remainingGroups.isEmpty =>
        val lambda = new core.prim._commandlambda(block.arguments)
        lambda.token = block.openBracket
        val blockArg = commandBlockWithStatements(block.group.location, stmts.stmts)
        val ra = new core.ReporterApp(lambda, Seq(blockArg), block.group.location)
        SuccessfulParse(PartialReporterApp(ra))
      case (PartialStatements(stmts), remainingGroups) => fail(ExpectedCommand, remainingGroups.head.start)
      case (other, remainingGroups)                    => fail(ExpectedCommand, block.sourceLocation)
    } match {
      case f: FailedParse => PartialError(f)
      case SuccessfulParse(partial) => partial
    }
  }

  // TODO: this is an innefficient and inelegant solution which transforms a higher-information
  // data stracture (List[SyntaxGroup]) to a lower (Iterator[Token]), and uses Exceptions for flow
  // control (within LiteralParser)
  def processLiteralList(block: DelayedBlock): Partial = {
    // It's OK to pass the NullImportHandler here because this code is only used when
    // parsing literal lists while compiling code.
    // When reading lists from export files and such LiteralParser is used
    // via Compiler.readFromString. ev 3/20/08, RG 08/09/16

    ParseResult.fromTry(
      Try {
        // this token-iterator may need adjustment...
        val (list, closeBracket) =
          new LiteralParser(NullImportHandler).parseLiteralList(block.openBracket, block.group.allTokens.drop(1).iterator)
        val tmp = new core.prim._const(list)
        tmp.token = new Token("", TokenType.Literal, null)(block.group.location)
        new core.ReporterApp(tmp, block.group.location)
      }) match {
      case f: FailedParse => PartialError(f)
      case SuccessfulParse(partial) => PartialReporterApp(partial)
    }
  }

  def groupSyntax(tokens: BufferedIterator[Token]): ParseResult[Seq[SyntaxGroup]] = {
    val tokenTpeMatch = Map[TokenType, TokenType](
      TokenType.CloseBracket -> TokenType.OpenBracket,
      TokenType.CloseParen -> TokenType.OpenParen)
    def delimName(tpe: TokenType) =
      if (tpe == TokenType.OpenParen || tpe == TokenType.CloseParen) "parenthesis" else "bracket"
    @tailrec
    def groupRec(acc: Seq[SyntaxGroup], stack: List[Token], groupStack: List[List[SyntaxGroup]]): ParseResult[Seq[SyntaxGroup]] = {
      if (! tokens.hasNext) SuccessfulParse(acc)
      else {
        val thisToken = tokens.next()
        thisToken.tpe match {
          case (TokenType.OpenParen | TokenType.OpenBracket) =>
            groupRec(acc, thisToken :: stack, List.empty[SyntaxGroup] :: groupStack)
          case tpe@(TokenType.CloseParen | TokenType.CloseBracket) if stack.headOption.exists(_.tpe == tokenTpeMatch(tpe)) =>
            val groupTokens = groupStack.head.reverse

            val group =
              if (tpe == TokenType.CloseParen) ParenGroup(groupTokens,   stack.head, thisToken)
              else                             BracketGroup(groupTokens, stack.head, thisToken)

            if (stack.tail.isEmpty) groupRec(group +: acc, stack.tail, groupStack.tail)
            else                    groupRec(acc, stack.tail, (group :: groupStack.tail.head) :: groupStack.tail.tail)
          case tpe@(TokenType.CloseParen | TokenType.CloseBracket) if stack.nonEmpty =>
            fail(s"Expected close ${delimName(stack.head.tpe)} here", thisToken)
          case (TokenType.CloseParen | TokenType.CloseBracket) =>
            fail(ExpectedCommand, thisToken)
          case TokenType.Eof if stack.nonEmpty =>
            if ((acc ++ groupStack.head).exists(_.allTokens.head.tpe == TokenType.Command)) {
              fail(s"No closing ${delimName(stack.head.tpe)} for this open ${delimName(stack.head.tpe)}.", stack.head)
            } else
              fail(ExpectedCommand, stack.head)
          case _ if stack.isEmpty => groupRec(Atom(thisToken) +: acc, stack, groupStack)
          case _ => groupRec(acc, stack, (Atom(thisToken) :: groupStack.head) :: groupStack.tail )
        }
      }
    }
    groupRec(Seq(), List.empty[Token], Nil).map(_.reverse)
  }

  private def traverse[A](elems: Seq[ParseResult[A]]): ParseResult[Seq[A]] = {
    elems.foldLeft(ParseResult(Seq.empty[A])) {
      case (acc, SuccessfulParse(a))    => acc.map { as => as :+ a }
      case (f@FailedParse(_), _) => f
      case (acc, f@FailedParse(_)) => f
    }
  }

  /**
   * resolves the types of all arguments in a given application.  See comments for resolveType() for
   * an explanation of terminology.  This is a slightly ugly bit of code. The ugliness comes from
   * variadic primitives, which make trouble for a simple left-to-right approach. We rely on the
   * fact that variadic prims can only be variadic in one argument, and check types from the left
   * until we possibly encounter a variadic arg, at which point we jump to the right end of the args
   * and check types backwards. Finally, once we've isolated any args that must match the variadic
   * type, we check those left-to-right. There's one other bit of ugliness at the beginning
   * pertaining to left-hand args to infix operators.
   */
  private def resolveTypes(
    untypedArgs: Seq[core.Expression],
    argContext: ArgumentParseContext,
    scope: SymbolTable): ParseResult[Seq[core.Expression]] = {

    val syntax = argContext.syntax
    val location = argContext.sourceLocation
    val displayName = argContext.displayName
    val typedArgs = scala.collection.mutable.Seq[core.Expression](untypedArgs: _*)
    val formalTypes = if (syntax.isInfix) syntax.left +: syntax.right else syntax.right

    def typeArg(i: Int, arg: core.Expression): ParseResult[core.Expression] =
      resolveType(i, arg, displayName, scope)

    var index = 0
    val types = syntax.right
    val repeatedIndex = formalTypes.indexWhere(t => compatible(Syntax.RepeatableType, t))

    // first look at left arg, if any
    if (syntax.isInfix && untypedArgs.size < 2) {
      fail(argContext.missingInput(0), location)
    } else if (repeatedIndex == -1) {
      val checkedTypes =
        if (formalTypes.lastOption.exists(t => compatible(Syntax.OptionalType, t)) && formalTypes.length - 1 == untypedArgs.size) {
          // only need to check the first (length - 1) arguments
          formalTypes.init
        } else {
          // We know the that the arguments must match the formalTypes exactly.
          // This involves making sure that the number of arguments is as expected and all arguments match their expected types
          formalTypes
        }

      if (untypedArgs.length < checkedTypes.length)
        fail(argContext.missingInput(untypedArgs.length), location)
      else {
        val argsWithTypes = formalTypes zip untypedArgs
        traverse(argsWithTypes.map((typeArg _).tupled))
      }
    } else if (untypedArgs.size < repeatedIndex) {
      fail(argContext.missingInput(index), location)
    } else {
      val zippedArgs = (formalTypes zip untypedArgs)

      // Check the first (formalTypes.length - 1) arguments,
      val nonRepeatingArgs = zippedArgs.take(repeatedIndex)
      val resolvedNonRepeatingArgs = traverse(nonRepeatingArgs.map((typeArg _).tupled))

      index = repeatedIndex
      // Then check the remaining (untypedArgs.length - (formalTypes.length - 1)) arguments to match the repeatable syntax type
      var actual1 = index
      val formal1 = index
      if (formal1 < types.length) {
        // then we encountered a repeatable arg, so we look at right args from right-to-left...
        var actual2 = untypedArgs.size - 1
        var formal2 = types.length - 1
        while (formal2 >= 0 && !compatible(Syntax.RepeatableType, types(formal2))) {
          if (untypedArgs.size <= actual2 || actual2 <= -1) {
            return fail(argContext.missingInput(actual2), location)
          } else {
            typedArgs.update(actual2, resolveType(formalTypes(actual2), untypedArgs(actual2), displayName, scope).get)
            formal2 -= 1
            actual2 -= 1
          }
        }
        // now we check any repeatable args...
        while (actual1 <= actual2) {
          typedArgs.update(actual1, resolveType(types(formal1), untypedArgs(actual1), displayName, scope).get)
          actual1 += 1
        }
      }
      resolvedNonRepeatingArgs.map(_ ++ scala.collection.immutable.Seq[core.Expression](typedArgs: _*).drop(repeatedIndex))
    }
  }

  private def syntheticCommandBlock(args: Seq[core.Expression], location: SourceLocation): core.CommandBlock = {
    val file = location.filename
    val blockLocation = args.lastOption.map(_.end) getOrElse location.end
    new core.CommandBlock(
      new core.Statements(file),
      SourceLocation(blockLocation, blockLocation, file), true)
  }

  /**
   * this is used for generating an error message when some arguments are found to be missing
   */
  private def missingInput(syntax: Syntax, displayName: String, argumentIndex: Int): String = {
    val right = argumentIndex >= 1 || (! syntax.isInfix)
    lazy val inputName          = if (syntax.rightDefault > 1) "inputs"        else "input"
    lazy val variadicQuantifier = if (syntax.isVariadic)       " at least"     else ""
    lazy val infixQuantifier    = if (syntax.isInfix)          " on the right" else ""

    val variadicMessage = right && syntax.isVariadic && syntax.minimum == 0
    val result =
      if (variadicMessage) s"$displayName expected ${syntax.rightDefault} $inputName on the right or any number of inputs when surrounded by parentheses"
      else if (right)      s"$displayName expected$variadicQuantifier ${syntax.rightDefault} $inputName$infixQuantifier"
      else                 s"$displayName expected ${core.TypeNames.aName(syntax.left)} on the left"

    lazy val rightArgs          = syntax.right.map(core.TypeNames.aName(_).replaceFirst("anything", "any input"))

    if (!right)
      result
    else if (rightArgs.forall(_ == "any input"))
      s"$result."
    else if (rightArgs.size == 1)
      s"$result, ${rightArgs.mkString}."
    else
      s"$result, ${rightArgs.toList.dropRight(1).mkString(", ")} and ${rightArgs.last}."
  }

  /**
   * resolves the type of an expression. We call this "resolution" instead of "checking" because
   * sometimes the expression needs further parsing or processing depending on its context and
   * expected type. For example, delayed blocks need to be parsed here based on what they're
   * expected to be. The caller should replace the expr it passed in with the one returned,
   * as it may be different.
   */
  private def resolveType(goalType: Int, arg: core.Expression, instruction: String, scope: SymbolTable): ParseResult[core.Expression] = {
    if (goalType == Syntax.ReporterType) {
      arg match {
        case ra: core.ReporterApp => SuccessfulParse(ra)
        case _ => fail(ExpectedReporter, arg)
      }
    } else if (compatible(goalType, arg.reportedType)) SuccessfulParse(arg)
    else {
      // remove reference type from message unless it's part of the goalType, confusing to see
      // "expected a variable or a number"
      val displayedReportedType = {
        if ((goalType & Syntax.ReferenceType) == 0 && ((arg.reportedType & ~Syntax.ReferenceType) != 0))
          arg.reportedType & ~Syntax.ReferenceType
        else
          arg.reportedType
      }
      val message =
        s"$instruction expected this input to be ${core.TypeNames.aName(goalType)}, but got ${core.TypeNames.aName(displayedReportedType)} instead"
      FailedParse(new TypeMismatch(arg, message, goalType, arg.reportedType))
    }
  }

    /*
  // _minus is allowed to be unary (negation) only if it's missing a left argument and
  // in a possibly variadic context (the first thing in a set of parens, basically).
  case _: core.prim._minus if variadic =>
    val r2 = new core.prim._unaryminus
    r2.token = token
    finalizeReporterApp(new core.ReporterApp(r2, token.sourceLocation))
    */

  private def syntheticVariables(count: Int, token: Token, scope: SymbolTable): (Seq[String], Seq[core.ReporterApp]) = {
    val (varNames, _) = (1 to count).foldLeft((Seq[String](), scope)) {
      case ((acc, s), _) =>
        val (varName, newScope) = s.withFreshSymbol(SymbolType.LambdaVariable)
        (acc :+ varName, newScope)
    }
    val varApps = varNames.map { vn =>
      val lv = new core.prim._lambdavariable(vn, synthetic = true)
      lv.token = Token(vn, TokenType.Reporter, lv)(token.sourceLocation)
      new core.ReporterApp(lv, lv.token.sourceLocation)
    }
    (varNames, varApps)
  }

  /**
   * handle the case of the concise lambda syntax, where I can write e.g. "map + ..." instead
   * of "map [[x y] -> x + y] ...".  for the lambda primitive itself we allow this even for literals
   *  and nullary reporters, for the other primitives like map we require the reporter to
   *  take at least one input (since otherwise a simple "map f xs" wouldn't evaluate f).
   */
  private def expandConciseReporterLambda(rApp: core.ReporterApp, reporter: core.Reporter, scope: SymbolTable): core.ReporterApp = {
    val (varNames, varApps) = syntheticVariables(reporter.syntax.totalDefault, reporter.token, scope)
    val lambda = new core.prim._reporterlambda(Lambda.ConciseArguments(varNames))
    lambda.token = reporter.token
    new core.ReporterApp(lambda, Seq(rApp.withArguments(varApps)), reporter.token.sourceLocation)
  }


  // expand e.g. "foreach xs print" -> "foreach xs [[x] -> print x]"
  private def expandConciseCommandLambda(token: Token, scope: SymbolTable): ParseResult[core.ReporterApp] = {
    val coreCommand = token.value.asInstanceOf[core.Command]
    if (! coreCommand.syntax.canBeConcise)
      FailedParse(new UnexpectedTokenFailure(token))
    else {
      val (varNames, varApps) = syntheticVariables(coreCommand.syntax.totalDefault, coreCommand.token, scope)
      val stmtArgs =
        if (coreCommand.syntax.takesOptionalCommandBlock)
          // synthesize an empty block so that later phases of compilation will be dealing with a
          // consistent number of arguments - ST 3/4/08
          varApps :+ new core.CommandBlock(new core.Statements(token.filename), token.sourceLocation, synthetic = true)
        else varApps

      val lambda = new core.prim._commandlambda(Lambda.ConciseArguments(varNames))
      lambda.token = token

      val stmt = new core.Statement(coreCommand, stmtArgs, token.sourceLocation)

      val commandBlock = commandBlockWithStatements(token.sourceLocation, Seq(stmt), synthetic = true)

      SuccessfulParse(new core.ReporterApp(lambda, Seq(commandBlock), token.sourceLocation))
    }
  }

  private def commandBlockWithStatements(sourceLocation: SourceLocation, stmts: Seq[core.Statement], synthetic: Boolean = false) = {
    val statements = new core.Statements(sourceLocation.filename, stmts)
    new core.CommandBlock(statements, sourceLocation, synthetic)
  }

  // these are most of the compiler error messages. the ones actually in the code are those
  // that require some substitution, which are pretty much only type errors currently.
  private val ExpectedCommand = "Expected command."
  // private val ExpectedCloseBracket = "Expected closing bracket."
  private val ExpectedCloseParen = "Expected a closing parenthesis here."
  private val ExpectedReporter = "Expected reporter."
  private val InvalidVariadicContext =
    "To use a non-default number of inputs, you need to put parentheses around this."
  // private val MissingCloseBracket = "No closing bracket for this open bracket."
  // private val MissingCloseParen = "No closing parenthesis for this open parenthesis."
  private val MissingInputOnLeft = "Missing input on the left."

}
