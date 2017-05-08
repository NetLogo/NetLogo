// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ prim, FrontEndProcedure, I18N, SourceLocatable, SourceLocation, Syntax, Token, TokenType },
    prim.Lambda,
    Syntax.compatible

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

  sealed trait Partial {
    // Primacy gives the order in which partials ought to be reduced.
    // Where possible, we collapse a command to statement before parsing the next command
    def primacy: Int
    def needsArguments: Boolean = false
  }

  case class PartialProcDef(pd: core.ProcedureDefinition) extends Partial {
    val primacy = 8
  }
  case class PartialStatements(stmts: core.Statements) extends Partial {
    val primacy = 7
  }
  case class PartialStatement(stmt: core.Statement) extends Partial {
    val primacy = 6
  }
  case class PartialCommandAndArgs(cmd: core.Command, tok: Token, args: Seq[core.Expression]) extends Partial {
    val primacy = 5
    override def needsArguments =
      cmd.syntax.totalDefault < args.length
  }
  case class PartialReporterAndArgs(rep: core.Reporter, tok: Token, args: Seq[core.Expression]) extends Partial {
    val primacy = 4
    override def needsArguments =
      rep.syntax.totalDefault < args.length
  }
  // this one is particularly odd, a raw command can *sometimes* end up being an Argument,
  // but sometimes ends up being the start of a statement
  case class PartialCommand(cmd: core.Command, tok: Token) extends Partial {
    val primacy = 3
  }
  case class PartialReporterApp(app: core.ReporterApp) extends Partial {
    val primacy = 2
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

  // def runShiftReduce(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
  def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
    def reduce(stack: List[Partial]): List[Partial] = {
      println(stack.reverse.mkString("//"))
      val secondFromTop = if (stack.length < 2) None else stack(1)
      stack match {
        case PartialStatement(stmt) :: Nil =>
          PartialStatements(new core.Statements(procedureDeclaration.filename, Seq(stmt))) :: Nil
        case PartialStatement(s) :: PartialStatements(stmts) :: rest =>
          // end *may* not be correct here when there is a block
          PartialStatements(stmts.copy(stmts = stmts.stmts :+ s)) :: rest
        case PartialStatements(stmts) :: Nil =>
          // TODO: 100 is not really the end, just not sure how we'll get the number yet.
          PartialProcDef(new core.ProcedureDefinition(procedureDeclaration, stmts, 100)) :: Nil
        case PartialReporter(rep, tok) :: rest =>
          if (rep.syntax.rightDefault == 0)
            PartialReporterApp(new core.ReporterApp(rep, Seq(), tok.sourceLocation)) :: rest
          else
            PartialReporterAndArgs(rep, tok, Seq()) :: rest
        case PartialCommand(cmd, tok) :: Nil =>
          PartialCommandAndArgs(cmd, tok, Seq()) :: Nil
        case PartialCommandAndArgs(cmd, tok, args) :: rest =>
          resolveTypes(args, ArgumentParseContext(cmd, tok.sourceLocation), scope) match {
            case f: FailedParse => List(PartialError(f))
            case SuccessfulParse(typedArgs) =>
              val loc = SourceLocation(tok.start, args.lastOption.map(_.sourceLocation.end).getOrElse(tok.end), tok.filename)
              PartialStatement(new core.Statement(cmd, typedArgs, loc)) :: rest
          }
        case PartialInfixReporter(iRep, iTok) :: PartialReporterApp(app) :: rest =>
          PartialReporterAndArgs(iRep, iTok, Seq(app)) :: rest
        case PartialInfixReporter(iRep, iTok) :: rest =>
          List(PartialError(fail(ArgumentParseContext(iRep, iTok.sourceLocation).missingInput(0), iTok.sourceLocation)))
        case PartialReporterApp(arg) :: PartialCommandAndArgs(cmd, tok, args) :: rest =>
          PartialCommandAndArgs(cmd, tok, args :+ arg) :: rest
        case PartialReporterApp(app) :: PartialCommand(cmd, tok) :: rest =>
          PartialCommandAndArgs(cmd, tok, Seq(app)) :: rest
        case (ra@PartialReporterApp(_)) :: PartialReporter(rep, tok) :: rest =>
          ra :: PartialReporterAndArgs(rep, tok, Seq()) :: rest
        case PartialReporterApp(app) :: PartialReporterAndArgs(rep, tok, args) :: rest =>
          // we aren't yet handling variadics properly
          PartialReporterAndArgs(rep, tok, args :+ app) :: rest
        case PartialReporterAndArgs(rep, tok, args) :: rest if rep.syntax.rightDefault <= args.length =>
          resolveTypes(args, ArgumentParseContext(rep, tok.sourceLocation), scope) match {
            case f: FailedParse => List(PartialError(f))
            case SuccessfulParse(typedArgs) =>
              val loc = SourceLocation(tok.start, args.lastOption.map(_.sourceLocation.end).getOrElse(tok.end), tok.filename)
              PartialReporterApp(new core.ReporterApp(rep, typedArgs, loc)) :: rest
          }
        // this will eventually need to recur on DelayedBlock
        case PartialDelayedBlock(db) :: PartialCommand(cmd, tok) :: rest =>
          val blockResult = parseDelayedBlock(db, cmd.syntax.right.head, scope).get
          PartialCommandAndArgs(cmd, tok, Seq(blockResult)) :: rest
        case _ => List(PartialError(fail("unknown parse for: " + stack.reverse.mkString(" // "), SourceLocation(0, 0, ""))))
      }
    }

    def shouldShift(p: Partial, g: SyntaxGroup): Boolean = {
      val stackPrimacy = p.primacy
      (p, g) match {
        case (_, pg: ParenGroup)   => true
        case (_, bg: BracketGroup) => true
        case (_, Atom(token@Token(_, TokenType.Command, cmd: core.Command))) =>
          stackPrimacy > 6
        case (p, Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter))) =>
          p.needsArguments || rep.syntax.isInfix || stackPrimacy > 3
        case (p, Atom(token@Token(_, TokenType.Literal, _))) =>
          p.needsArguments || stackPrimacy > 2
        case o => throw new NotImplementedError(s"shift precedence undefined for $o")
      }
    }

    def shift(g: SyntaxGroup): Partial = {
      g match {
        case Atom(token@Token(_, TokenType.Command, cmd: core.Command)) =>
          PartialCommand(cmd, token)
        case Atom(token@Token(_, TokenType.Literal, literalVal)) =>
          val coreReporter = new core.prim._const(token.value)
          token.refine(coreReporter)
          val newReporterApp = new core.ReporterApp(coreReporter, token.sourceLocation)
          PartialReporterApp(newReporterApp)
        case Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter)) if rep.syntax.isInfix =>
          PartialInfixReporter(rep, token)
        case Atom(token@Token(_, TokenType.Reporter, rep: core.Reporter)) =>
          PartialReporter(rep, token)
        case bg: BracketGroup =>
          PartialDelayedBlock(DelayedBlock(bg, scope))
        case ParenGroup(inner, start, end) =>
          val intermediateResult = runRec(Nil, inner, scope)
          intermediateResult match {
            case SuccessfulParse((p, Seq())) => p
            case SuccessfulParse((p, Seq(g, _*))) => PartialError(fail(ExpectedCommand, g.location))
            case f: FailedParse => PartialError(f)
          }
        case other => throw new NotImplementedError(s"shift undefined for $other")
      }
    }

    def runRec(stack: List[Partial], groups: Seq[SyntaxGroup], scope: SymbolTable): RemainingParseResult[Partial] = {
      if (groups.isEmpty) {
        if (stack.length > 1)
          runRec(reduce(stack), groups, scope)
        else
          stack.headOption match {
            case Some(PartialError(failure)) => failure
            case Some(ppd: PartialProcDef)   => SuccessfulParse((ppd, Seq()))
            case Some(_)                     => runRec(reduce(stack), groups, scope)
            case None                        => fail("Parse error, trying to reduce empty stack!", new core.SourceLocation(0, 12040192, ""))
          }
      } else {
        stack.headOption match {
          case Some(PartialError(failure: FailedParse)) => failure
          case Some(p) =>
            if (shouldShift(p, groups.head)) {
              runRec(shift(groups.head) :: stack, groups.tail, scope)
            } else {
              runRec(reduce(stack), groups, scope)
            }
          case None =>
            runRec(shift(groups.head) :: stack, groups.tail, scope)
        }
      }
    }

    //.init to avoid Eof awkwardness
    runRec(Nil, groupSyntax(tokens.buffered).get.init, scope).get match {
      case (PartialError(f), _)=> throw f.failure.toException
      case (PartialProcDef(pd), _) => pd
      case other => throw new Error("bad parse?! " + other)
    }
  }

  /**
   * parses a procedure. Procedures are a bunch of statements (not a block of statements, that's
   * something else), and so are parsed as such. */
  def applyOld(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
  // def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
    val buffered = tokens.buffered
    val head = buffered.head
    val groupedSyntax = groupSyntax(buffered)
    val statementList =
      groupedSyntax.flatMap { groupedTokens =>
        parseStatements(groupedTokens, ParseContext(false, scope), TokenType.Eof, parseStatement(_, _)).map(_._1)
      }.get

    val stmts = new core.Statements(head.filename, statementList, false)
    val end = groupedSyntax.get.last.start.start // set the end as the start of the end token
    new core.ProcedureDefinition(procedureDeclaration, stmts, end)
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

  // TODO: We no longer need the terminator, since SyntaxGroup takes care of that for us
  private def parseStatements(
    groups: Seq[SyntaxGroup],
    context: ParseContext,
    terminator: TokenType,
    f: (Seq[SyntaxGroup], ParseContext) => ParseResult[(core.Statement, Seq[SyntaxGroup], SymbolTable)]): RemainingParseResult[Seq[core.Statement]] = {
      val b = Buffer[core.Statement]()
      var remainingGroups = groups
      var activeContext = context
      var badStmt = Option.empty[FailedParse]

      while (badStmt.isEmpty && remainingGroups.nonEmpty && ! isEnd(remainingGroups.head, terminator)) {
        f(remainingGroups, activeContext) match {
          case SuccessfulParse((stmt, newGroups, newScope)) =>
            remainingGroups = newGroups
            activeContext = activeContext.withScope(newScope)
            b += stmt
          case FailedParse(failure) =>
            badStmt = Some(FailedParse(failure))
        }
      }
      badStmt.getOrElse(SuccessfulParse((b.toSeq, remainingGroups)))
  }

  /**
   * parses a statement.
   *
   * @return the parsed statement and the symbol table resulting from any added scope
   */
  private def parseStatement(groups: Seq[SyntaxGroup], parseContext: ParseContext): ParseResult[(core.Statement, Seq[SyntaxGroup], SymbolTable)] = {
    groups.head match {
      case pg@ParenGroup(subgroups, open, close) =>
        parseStatement(subgroups, parseContext.withVariadic(true)).flatMap {
          case (stmt, remainingGroups, newScope) =>
            if (remainingGroups.nonEmpty)
              fail(ExpectedCloseParen,
                SourceLocation(remainingGroups.head.start.start, remainingGroups.head.start.end, remainingGroups.head.start.filename))
            else
              SuccessfulParse((stmt.changeLocation(pg.location), groups.tail, newScope))
        }
      case Atom(token@Token(_, TokenType.Command, coreCommand: core.Command)) =>
        val nameToken = groups.tail.headOption match {
          case Some(Atom(t)) if t.tpe != TokenType.Eof => Some(t)
          case _ => None
        }

        val (stmt, newScope, remainingGroups) =
          LetScope(coreCommand, nameToken, parseContext.scope).map {
            case (letCommand, newScope) =>
              (new core.Statement(letCommand, token.sourceLocation), newScope, groups.tail)
          }.getOrElse((new core.Statement(coreCommand, token.sourceLocation), parseContext.scope, groups.tail))

        parsePotentiallyVariadicArgumentList(remainingGroups, parseContext.withScope(newScope), ArgumentParseContext(stmt.command, stmt.sourceLocation)).map {
          case (arguments, groupsAfterArgumentList) =>
            (stmt.withArguments(arguments), groupsAfterArgumentList, newScope)
        }
      case other =>
        val token = other.allTokens.head
        token.value match {
          case (_: core.prim._symbol | _: core.prim._unknownidentifier) if ! parseContext.scope.contains(token.text.toUpperCase) =>
            fail(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
          case _ => fail(ExpectedCommand, token)
        }
    }
  }

  private def traverse[A](elems: Seq[ParseResult[A]]): ParseResult[Seq[A]] = {
    elems.foldLeft(ParseResult(Seq.empty[A])) {
      case (acc, SuccessfulParse(a))    => acc.map { as => as :+ a }
      case (f@FailedParse(_), _) => f
      case (acc, f@FailedParse(_)) => f
    }
  }


  private def liftResolveTypes(argContext: ArgumentParseContext, scope: SymbolTable)(
      untypedArgs: Seq[core.Expression], remainingGroups: Seq[SyntaxGroup]): RemainingParseResult[Seq[core.Expression]] = {
        fail("Don't use me!", remainingGroups.head.location)
        // resolveTypes(untypedArgs, argContext, scope).map(typedArgs => (typedArgs, remainingGroups))
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

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). The number is dictated by the syntax of the head instruction of the application
   * whose arguments we're parsing.
   */
  private def parseArguments(groups: Seq[SyntaxGroup], parseContext: ParseContext, argContext: ArgumentParseContext): RemainingParseResult[Seq[core.Expression]] = {
    import argContext.syntax.{ right, rightDefault, takesOptionalCommandBlock }

    def parseArgAt(index: Int)(acc: List[core.Expression], gs: Seq[SyntaxGroup]): RemainingParseResult[List[core.Expression]] = {
      parseArgExpression(gs, index, parseContext, argContext).map {
        case (exp, rest) => (exp :: acc, rest)
      }
    }

    val parsedArgs =
      (0 until rightDefault).foldLeft(ParseResult((List.empty[core.Expression], groups))) { (parseResult, i) =>
        parseResult.flatMap((parseArgAt(i min right.size - 1) _).tupled)
      }

    val argsInReverse =
      if (! takesOptionalCommandBlock) parsedArgs
      else
        parsedArgs.flatMap {
          case (args, newGroups) =>
            newGroups.headOption match {
              case Some(b@BracketGroup(_, _, _)) =>
                parseArgAt(right.size - 1)(args, Seq(b))
                  .map { case (args, restGroups) => (args, newGroups.tail) }
              // synthetic block so later phases of compilation have consistent number of arguments
              case other => SuccessfulParse((syntheticCommandBlock(args, argContext.sourceLocation) :: args, newGroups))
            }
        }
    argsInReverse.map { case (args, restOfGroups) => (args.reverse, restOfGroups) }
  }

  private def syntheticCommandBlock(args: Seq[core.Expression], location: SourceLocation): core.CommandBlock = {
    val file = location.filename
    val blockLocation = args.lastOption.map(_.end) getOrElse location.end
    new core.CommandBlock(
      new core.Statements(file),
      SourceLocation(blockLocation, blockLocation, file), true)
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). We'll continue parsing expressions until we encounter an error or
   * a lower precedence binary reporter. In the latter case, it turns out that this app
   * can't have a non-default number of args after all, so we assert that it doesn't. Type
   * resolution is then performed.
   */
  private def parseVarArgs(groups: Seq[SyntaxGroup], parseContext: ParseContext, argContext: ArgumentParseContext): RemainingParseResult[Seq[core.Expression]] = {
    import argContext.syntax.{ right, totalDefault }
    def parseArgument(groups: Seq[SyntaxGroup], argIndex: Int, parsedArgs: ParseResult[Seq[core.Expression]]): RemainingParseResult[Seq[core.Expression]] =
      groups.headOption match {
        // corresponds to end of parentheses
        case None                => parsedArgs.map(as => (as, Seq.empty[SyntaxGroup]))
        // NOTE: We used to check here whether we were at Eof, but since we group things that's no longer necessary
        case Some(Atom(Token(_, TokenType.Reporter, rep: core.Reporter))) if right(argIndex) != Syntax.ReporterType && rep.syntax.isInfix  =>
          // this if-statement is ugly....
          if (parsedArgs.exists(_.size == totalDefault))
            parsedArgs.map(as => (as, groups.tail))
          else
            fail(InvalidVariadicContext, argContext.sourceLocation)
        case other =>
          parsedArgs.flatMap { args =>
            parseArgExpression(groups, argIndex, parseContext, argContext).flatMap {
              case (newExp: core.Expression, remainingGroups: Seq[SyntaxGroup]) =>
                val newIndex = (right.length - 1) min (argIndex + 1)
                parseArgument(remainingGroups, newIndex, SuccessfulParse(args :+ newExp))
            }
          }
      }
    parseArgument(groups, 0, SuccessfulParse(Seq()))
  }

  private def parsePotentiallyVariadicArgumentList(groups: Seq[SyntaxGroup], parseContext: ParseContext, argContext: ArgumentParseContext): RemainingParseResult[Seq[core.Expression]] = {
    val untypedArgs =
      if (parseContext.variadic && argContext.variadic) parseVarArgs(groups, parseContext, argContext)
      else                                              parseArguments(groups, parseContext, argContext)
    untypedArgs.flatMap((liftResolveTypes(argContext, parseContext.scope) _).tupled)
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
  private def resolveType(goalType: Int, originalArg: core.Expression, instruction: String, scope: SymbolTable): ParseResult[core.Expression] = {
    // now that we know the type, finish parsing any blocks
    (originalArg match {
      case block: DelayedBlock => parseDelayedBlock(block, goalType, scope)
      case _                   => SuccessfulParse(originalArg)
    }).flatMap { arg =>
      if (compatible(goalType, arg.reportedType)) SuccessfulParse(arg)
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
  }

  /**
   * a wrapper around parseExpressionInternal for parsing expressions in argument position. Argument
   * expressions can never be variadic, so we don't need that arg.
   *
   * @param tokens     the input token stream
   * @param precedence the precedence of the operator currently on top of
   *                   the "stack"
   * @param app        the Application we're currently parsing args for.
   *                   Used to generate nice error messages.
   */
  private def parseArgExpression(
      groups:       Seq[SyntaxGroup],
      argIndex:     Int,
      parseContext: ParseContext,
      argContext:   ArgumentParseContext): RemainingParseResult[core.Expression] = {
    parseExpressionInternal(groups, parseContext.withVariadic(false), argContext.parseArgumentContext(argIndex)) recoverWith {
      case e: MissingPrefixFailure   => fail(MissingInputOnLeft, e.token)
      case e: UnexpectedTokenFailure =>
        fail(argContext.missingInput(0), argContext.sourceLocation)
    }
  }

  private def parseCompletely(
    groups: Seq[SyntaxGroup],
    failureMessage: String,
    parseContext: ParseContext,
    expressionParseContext: ExpressionParseContext): RemainingParseResult[core.Expression] = {
      parseExpressionInternal(groups, parseContext, expressionParseContext.copy(precedence = MinPrecedence))
        .recoverWith {
          case e: MissingPrefixFailure   => fail(MissingInputOnLeft, e.token)
          case e: UnexpectedTokenFailure => fail(ExpectedReporter, e.token)
        }
        .flatMap {
          case (arg, remainingGroups) =>
            if (remainingGroups.isEmpty) SuccessfulParse((arg, remainingGroups))
            else fail(failureMessage, remainingGroups.head.start)
        }
  }

  /**
   * parses an expression.
   *
   * Throws UnexpectedTokenException if it sees an unrecognized token, because this state of affairs
   * must be interpreted in a context-dependent way. It generally indicates ExpectedReporter or
   * MissingInputOnRight, and it's up to the caller to interpret it properly.
   *
   * The same goes for MissingPrefixException, which indicates that an infix operator has been
   * encountered with no left argument.
   *
   * @param tokens     the input token stream
   * @param variadic   whether to treat this expression as possibly variadic
   * @param precedence the precedence of the operator currently on top of
   *                   the "stack"
   *
   * @throws MissingPrefixException if an infix operator is seen with no left argument.
   * @throws UnexpectedTokenException if an unexpected token is seen
   */
  private def parseExpressionInternal(
      groups: Seq[SyntaxGroup],
      parseContext: ParseContext,
      expressionParseContext: ExpressionParseContext): RemainingParseResult[core.Expression] = {
    import parseContext.{ variadic, scope }
    import expressionParseContext.{ goalType, precedence }

    val wantAnyLambda = goalType == (Syntax.ReporterType | Syntax.CommandType)
    val wantReporterLambda = wantAnyLambda || goalType == Syntax.ReporterType
    val wantCommandLambda = wantAnyLambda || goalType == Syntax.CommandType
    def finalizeReporterApp(rApp: core.ReporterApp): RemainingParseResult[core.ReporterApp] = {
      val remainingGroups = groups.tail
      // the !variadic check is to prevent "map (f a) ..." from being misparsed.
      if (wantReporterLambda && !variadic &&
        ! wantsSymbolicValue(rApp.reporter) &&
        (wantAnyLambda || rApp.reporter.syntax.totalDefault > 0))
        SuccessfulParse((expandConciseReporterLambda(rApp, rApp.reporter, scope), remainingGroups))
      else // the normal case
        parsePotentiallyVariadicArgumentList(remainingGroups, parseContext, ArgumentParseContext(rApp.reporter, rApp.sourceLocation)).map {
          case (e, remainingGroups2) => (rApp.withArguments(e), remainingGroups2)
        }
    }
    val expr: RemainingParseResult[core.Expression] =
      groups.head match {
        case ParenGroup(inner, open, close) =>
          parseCompletely(inner, ExpectedCloseParen, parseContext.withVariadic(true), expressionParseContext)
            .setRest(groups.tail)
        case group: BracketGroup =>
          SuccessfulParse((DelayedBlock(group, scope), groups.tail))
        case Atom(token) =>
          token.tpe match {
            case TokenType.Literal =>
              val coreReporter = new core.prim._const(token.value)
              token.refine(coreReporter)
              finalizeReporterApp(new core.ReporterApp(coreReporter, token.sourceLocation))
            case TokenType.Reporter | TokenType.Command
                if compatible(goalType, Syntax.SymbolType) =>
              val symbol = new core.prim._symbol()
              token.refine(symbol)
              SuccessfulParse((new core.ReporterApp(symbol, token.sourceLocation), groups.tail))
            case TokenType.Reporter =>
              val coreReporter = token.value.asInstanceOf[core.Reporter]
              coreReporter match {
                case (_: core.prim._symbol | _: core.prim._unknownidentifier) =>
                  LetVariableScope(coreReporter, token, scope).map {
                    case (letReporter, newScope) =>
                      finalizeReporterApp(new core.ReporterApp(letReporter, token.sourceLocation))
                  }.getOrElse {
                    fail(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
                  }
                // "|| wantReporterLambda" is needed to enable concise syntax for infix reporters, e.g. "map + ..."
                case rep if ! rep.syntax.isInfix || wantReporterLambda =>
                  finalizeReporterApp(new core.ReporterApp(coreReporter, token.sourceLocation))
                // _minus is allowed to be unary (negation) only if it's missing a left argument and
                // in a possibly variadic context (the first thing in a set of parens, basically).
                case _: core.prim._minus if variadic =>
                  val r2 = new core.prim._unaryminus
                  r2.token = token
                  finalizeReporterApp(new core.ReporterApp(r2, token.sourceLocation))
                case _ =>
                  FailedParse(new MissingPrefixFailure(token))
              }
            case TokenType.Command if wantCommandLambda =>
              expandConciseCommandLambda(token, scope).map(lambda => (lambda, groups.tail))
            case _ =>
              // here we throw a temporary exception, since we don't know yet what this error means... It
              // generally either means MissingInputOnRight or ExpectedReporter.
              FailedParse(new UnexpectedTokenFailure(token))
          }
      }
    val r =
      if (compatible(goalType, Syntax.SymbolType))
        expr
      else
        expr.flatMap {
          case (e, groups) => parseMore(e, groups, precedence, scope)
        }
    r
  }

  private def syntheticVariables(count: Int, token: Token, scope: SymbolTable): (Seq[String], Seq[core.ReporterApp]) = {
    val (varNames, _) = (1 to count).foldLeft((Seq[String](), scope)) {
      case ((acc, s), _) =>
        val (varName, newScope) = s.withFreshSymbol(SymbolType.LambdaVariable)
        (acc :+ varName, newScope)
    }
    val varApps = varNames.map { vn =>
      val lv = new core.prim._lambdavariable(vn, synthetic = true)
      lv.token = token
      new core.ReporterApp(lv, SourceLocation(token.start, token.end, token.filename))
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

  /**
   * possibly parses the rest of the current expression. Expressions are parsed in a slightly
   * strange way. First we parse an expr, then we look ahead to see if the next token is an infix
   * operator. If so, we use what we've seen so far as its first arg, and then parse its other
   * arguments. Then we repeat. The result of all of this is the actual expr.
   */
  def parseMore(originalExpr: core.Expression, groups: Seq[SyntaxGroup], precedence: Int, scope: SymbolTable): RemainingParseResult[core.Expression] = {
      def continueParsing(coreReporter: core.Reporter): Boolean = {
        val syntax = coreReporter.syntax
        syntax.isInfix && (syntax.precedence > precedence ||
          (syntax.isRightAssociative && syntax.precedence == precedence))
      }

      def reporterToApp(expr: core.Expression, coreReporter: core.Reporter, token: Token): core.ReporterApp = {
        val sourceLocation = SourceLocation(expr.start, token.end, token.filename)
        new core.ReporterApp(coreReporter, Seq(), sourceLocation)
      }

      def parseMoreRec(tExpr: RemainingParseResult[core.Expression]): RemainingParseResult[core.Expression] = {
        tExpr.flatMap {
          case (expr, groups) =>
            groups.headOption match {
              case Some(Atom(token@Token(_, TokenType.Reporter, coreReporter: core.Reporter))) if continueParsing(coreReporter) =>
                val rApp = reporterToApp(expr, coreReporter, token)
                val newExpr =
                  parseArguments(groups.tail, ParseContext(false, scope), ArgumentParseContext(rApp.reporter, rApp.sourceLocation))
                    .mapResult(expr +: _)
                    .flatMap((liftResolveTypes(ArgumentParseContext(rApp.reporter, rApp.sourceLocation), scope) _).tupled)
                    .mapResult(typedArgs =>
                        rApp.copy(args = typedArgs, location = rApp.sourceLocation.copy(end = typedArgs.last.end)))
                parseMoreRec(newExpr)
              case _ => tExpr
            }
        }
      }

      parseMoreRec(SuccessfulParse((originalExpr, groups)))
    }

  /**
   * parses a block (i.e., anything in brackets). This deals with reporter blocks (a single
   * expression), command blocks (statements), and literal lists (any number of literals). The
   * initial opening bracket should still be the first token in the tokens in the DelayedBlock.
   */
  private def parseDelayedBlock(block: DelayedBlock, goalType: Int, scope: SymbolTable): ParseResult[core.Expression] = {
    def statementList(block: DelayedBlock, scope: SymbolTable): ParseResult[Seq[core.Statement]] = {
      parseStatements(block.bodyGroups, ParseContext(false, block.internalScope), TokenType.CloseBracket, parseStatement(_, _))
        .flatMap {
          case (stmts, rest) =>
            // if we haven't gotten everything, we complain
            if (!rest.isEmpty) fail(ExpectedCommand, block.openBracket)
            else               SuccessfulParse(stmts)
        }
    }

    def reporterApp(block: DelayedBlock, expressionGoal: Int, scope: SymbolTable): ParseResult[core.ReporterApp] = {
      parseCompletely(block.bodyGroups, ExpectedCloseBracket, ParseContext(false, scope), ExpressionParseContext(expressionGoal, MinPrecedence))
        .flatMap {
          case (exp, remainingGroups) =>
            resolveType(Syntax.WildcardType, exp, null, scope).flatMap {
              case (expr: core.ReporterApp) => SuccessfulParse(expr)
              case (other: core.Expression) => fail(ExpectedCommand, other)
            }
        }
    }

    // This is probably the most complex part of the code.
    // We have to determine the type of a bracket-delimited block.
    // The basic rules:
    //   - If the block has an arrow, it must be an arrow lambda
    //   - If the goal type is compatible with a reporter/command block, it must be a reporter/command block
    //   - If the goal type cannot be a list and may be a reporter or command, it's parsed as a reporter/command lambda
    //   - If the goal type is a code block, it's read as a code block
    //   - If the goal type is a list, it's read as a list
    //   - If none of the above apply, we error

    val listNotWanted = ! compatible(goalType, Syntax.ListType)

    def buildReporterLambda(args: Lambda.Arguments) = {
      for {
        expr <- reporterApp(block, Syntax.WildcardType, block.internalScope)
      } yield {
        val lambda = new core.prim._reporterlambda(args)
        lambda.token = block.openBracket
        new core.ReporterApp(lambda, Seq(expr), block.group.location)
      }
    }

    def buildCommandLambda(args: Lambda.Arguments) = {
      for {
        stmtList <- statementList(block, block.internalScope)
      } yield {
        val lambda = new core.prim._commandlambda(args)
        lambda.token = block.openBracket
        val blockArg = commandBlockWithStatements(block.group.location, stmtList)
        new core.ReporterApp(lambda, Seq(blockArg), block.group.location)
      }
    }

    if (compatible(goalType, Syntax.CodeBlockType))
      parseCodeBlock(block)
    else if (block.isArrowLambda && ! block.isCommand)
      buildReporterLambda(block.asInstanceOf[ArrowLambdaBlock].arguments)
    else if (block.isArrowLambda && block.isCommand)
      buildCommandLambda(block.asInstanceOf[ArrowLambdaBlock].arguments)
    else if (compatible(goalType, Syntax.ReporterBlockType)) {
      for {
        expr <- reporterApp(block, goalType, scope)
      } yield new core.ReporterBlock(expr, block.group.location)
    } else if (compatible(goalType, Syntax.CommandBlockType)) {
      for {
        stmtList <- statementList(block, scope)
      } yield commandBlockWithStatements(block.group.location, stmtList)
    }
    else if (compatible(goalType, Syntax.ReporterType) && !block.isCommand && listNotWanted)
      buildReporterLambda(Lambda.NoArguments(false))
    else if (compatible(goalType, Syntax.CommandType) && block.isCommand && listNotWanted)
      buildCommandLambda(Lambda.NoArguments(false))
    else if (compatible(goalType, Syntax.ListType)) {
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
        }
        )
    }
    // we weren't actually expecting a block at all!
    else
      fail(s"Expected ${core.TypeNames.aName(goalType)} here, rather than a list or block.", block)
  }

  private def parseCodeBlock(block: DelayedBlock): ParseResult[core.ReporterApp] = {
    // Because we don't parse the inside of the code block, precisely because we don't want to define
    // legality of code in terms of the standard NetLogo requirements, we simply gather the tokens and leave
    // it alone. FD 8/19/2015, RG 5/2/2017

    val tokens = block match {
      case alb: ArrowLambdaBlock => alb.allTokens
      case adl: AmbiguousDelayedBlock => adl.tokens
    }

      val tmp = new core.prim._constcodeblock(tokens.tail.dropRight(2))
      tmp.token = tokens.head
    SuccessfulParse(new core.ReporterApp(tmp, SourceLocation(tokens.head.start, block.end, tokens.head.filename)))
  }

  private def wantsSymbolicValue(reporter: core.Reporter) =
    Syntax.compatible(reporter.syntax.left, Syntax.CodeBlockType | Syntax.SymbolType) ||
      reporter.syntax.right.exists(arg => Syntax.compatible(arg, Syntax.CodeBlockType | Syntax.SymbolType))


  // these are most of the compiler error messages. the ones actually in the code are those
  // that require some substitution, which are pretty much only type errors currently.
  private val ExpectedCommand = "Expected command."
  private val ExpectedCloseBracket = "Expected closing bracket."
  private val ExpectedCloseParen = "Expected a closing parenthesis here."
  private val ExpectedReporter = "Expected reporter."
  private val InvalidVariadicContext =
    "To use a non-default number of inputs, you need to put parentheses around this."
  // private val MissingCloseBracket = "No closing bracket for this open bracket."
  // private val MissingCloseParen = "No closing parenthesis for this open parenthesis."
  private val MissingInputOnLeft = "Missing input on the left."

}
