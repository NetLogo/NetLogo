// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ prim, FrontEndProcedure, Fail, I18N, SourceLocation, Syntax, Token, TokenType },
    prim.Lambda,
    Fail.{ cAssert, exception, cTry, fail },
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

  /**
   * parses a procedure. Procedures are a bunch of statements (not a block of statements, that's
   * something else), and so are parsed as such. */
  def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
    val buffered = tokens.buffered
    val statementList = parseStatements(buffered, scope, TokenType.Eof, parseStatement(_, false, _)).get
    // tryStatementList.failed.foreach(_.printStackTrace())
    val stmts = new core.Statements(buffered.head.filename, statementList, false)
    val end =
      if (buffered.head.end < Int.MaxValue) buffered.head.start
      else stmts.end
    new core.ProcedureDefinition(procedureDeclaration, stmts, end)
  }

  private def parseStatements(
    tokens: BufferedIterator[Token],
    scope: SymbolTable,
    terminator: TokenType,
    f: (BufferedIterator[Token], SymbolTable) => Try[(core.Statement, SymbolTable)]): Try[Seq[core.Statement]] = {
    val b = Buffer[core.Statement]()
    var activeScope = scope
    var badStmt = Option.empty[Failure[Seq[core.Statement]]]
    while (badStmt.isEmpty && tokens.head.tpe != terminator) {
      f(tokens, activeScope) match {
        case Success((stmt, newScope)) =>
          activeScope = newScope
          b += stmt
        case Failure(ex) =>
          badStmt = Some(Failure(ex))
      }
    }
    badStmt.getOrElse(Try(b.toSeq))
  }

  /**
   * parses a statement.
   *
   * @return the parsed statement and the symbol table resulting from any added scope
   */
  private def parseStatement(tokens: BufferedIterator[Token], variadic: Boolean, scope: SymbolTable): Try[(core.Statement, SymbolTable)] = {
    val token = tokens.head
    token.tpe match {
      case TokenType.OpenParen =>
        parseParenthesized(tokens, parseStatement(_, true, scope), token.filename).map {
          case ((stmt, newScope), loc) => (stmt.changeLocation(loc), newScope)
        }
      case TokenType.Command =>
        tokens.next()
        val coreCommand = token.value.asInstanceOf[core.Command]
        val nameToken = if (tokens.head.tpe != TokenType.Eof) Some(tokens.head) else None
        val (stmt, newScope) =
          LetScope(coreCommand, nameToken, scope).map {
            case (letCommand, newScope) =>
              (new core.Statement(letCommand, token.sourceLocation), newScope)
          }.getOrElse((new core.Statement(coreCommand, token.sourceLocation), scope))
        for {
          arguments <- parsePotentiallyVariadicArgumentList(coreCommand.syntax, variadic, stmt, tokens, newScope)
        } yield (stmt.withArguments(arguments), newScope)
      case _ =>
        token.value match {
          case (_: core.prim._symbol | _: core.prim._unknownidentifier) if ! scope.contains(token.text.toUpperCase) =>
            fail(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
          case _ => fail(ExpectedCommand, token)
        }
    }
  }

  private def parsePotentiallyVariadicArgumentList(
    syntax: Syntax,
    variadicContext: Boolean,
    app: core.Application,
    tokens: BufferedIterator[Token],
    scope: SymbolTable): Try[Seq[core.Expression]] = {
    val untypedArgs =
      if (variadicContext && syntax.isVariadic) parseVarArgs(syntax, app.sourceLocation, app.instruction.displayName, tokens, scope)
      else                                      parseArguments(syntax, app.sourceLocation, app.instruction.displayName, tokens, scope)

      for {
        parsedArgs       <- untypedArgs
        typedExpressions <- resolveTypes(syntax, parsedArgs, app.sourceLocation, app.instruction.displayName, scope)
      } yield typedExpressions
  }

  private def traverse[A](elems: Seq[Try[A]]): Try[Seq[A]] = {
    elems.foldLeft(Try(Seq.empty[A])) {
      case (acc, Success(a))    => acc.map { as => as :+ a }
      case (acc: Failure[Seq[A]], f: Failure[A]) => acc
      case (acc, Failure(e)) => Failure(e)
    }
  }

  // TODO: This may not be necessary
  private def traverseLazy[A, B](gen: () => Option[A], f: A => Try[B]): Try[Seq[B]] = {
    var next = gen()
    var result = Seq.empty[B]
    while (next.nonEmpty) {
      f(next.get) match {
        case Success(b) => result :+ b
        case Failure(e) => return Failure(e)
      }
      next = gen()
    }
    Success(result)
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
    syntax: Syntax,
    untypedArgs: Seq[core.Expression],
    location: SourceLocation,
    displayName: String,
    scope: SymbolTable): Try[Seq[core.Expression]] = {
    val typedArgs = scala.collection.mutable.Seq[core.Expression](untypedArgs: _*)
    val formalTypes = if (syntax.isInfix) syntax.left +: syntax.right else syntax.right

    def typeArg(i: Int, arg: core.Expression): Try[core.Expression] =
      resolveType(i, arg, displayName, scope)

    var index = 0
    val types = syntax.right
    val repeatedIndex = formalTypes.indexWhere(t => compatible(Syntax.RepeatableType, t))

    // first look at left arg, if any
    if (syntax.isInfix && untypedArgs.size < 2) {
      fail(missingInput(syntax, displayName, 0), location)
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

      if (untypedArgs.length != checkedTypes.length)
        fail(missingInput(syntax, displayName, untypedArgs.length), location)
      else {
        val argsWithTypes = formalTypes zip untypedArgs
        traverse(argsWithTypes.map((typeArg _).tupled))
      }
    } else if (untypedArgs.size <= repeatedIndex) {
      fail(missingInput(syntax, displayName, index), location)
    } else {
      val zippedArgs = (formalTypes zip untypedArgs)

      // Check the first (formalTypes.length - 1) arguments,
      val nonRepeatingArgs = zippedArgs.take(repeatedIndex)
      val resolvedNonRepeatingArgs = traverse(nonRepeatingArgs.map((typeArg _).tupled))

      index = repeatedIndex
      // Then check the remaining (untypedArgs.length - (formalTypes.length - 1)) arguments to match the repeatable syntax type
      var actual1 = index
      var formal1 = index
      if (formal1 < types.length) {
        // then we encountered a repeatable arg, so we look at right args from right-to-left...
        var actual2 = untypedArgs.size - 1
        var formal2 = types.length - 1
        while (formal2 >= 0 && !compatible(Syntax.RepeatableType, types(formal2))) {
          if (untypedArgs.size <= actual2 || actual2 <= -1) {
            return fail(missingInput(syntax, displayName, actual2), location)
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
   * whose arguments we're parsing. We expect exactly that number of args.  Type resolution is then
   * performed.
   */
  private def parseArguments(
    syntax: core.Syntax,
    sourceLocation: SourceLocation,
    displayName: String,
    tokens: BufferedIterator[Token],
    scope: SymbolTable): Try[Seq[core.Expression]] = {
    import syntax.{ right, takesOptionalCommandBlock }
    val parsedArgs =
      (0 until syntax.rightDefault).foldLeft(Try(List.empty[core.Expression])) { (t: Try[List[core.Expression]], i: Int) =>
        t.flatMap { acc =>
          parseArgExpression(syntax, tokens, sourceLocation, displayName, right(i min (right.size - 1)), scope).map { exp =>
            exp :: acc
          }
        }
      }
      .map(_.reverse)

    if (! takesOptionalCommandBlock) parsedArgs
    else {
      if (tokens.head.tpe == TokenType.OpenBracket)
        for {
          args     <- parsedArgs
          cmdBlock <- parseArgExpression(syntax, tokens, sourceLocation, displayName, right.last, scope)
        } yield args :+ cmdBlock
      else
        for {
          args <- parsedArgs
        } yield args :+ {
          // synthetic block so later phases of compilation have consistent number of arguments
          val file = tokens.head.filename
          val location = args.lastOption.map(_.end) getOrElse sourceLocation.end
          new core.CommandBlock(new core.Statements(file), SourceLocation(location, location, file), true)
        }
    }
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). We'll continue parsing expressions until we encounter a closing parenthesis, an
   * error, or a lower precedence binary reporter. In the last case, it turns out that this app
   * can't have a non-default number of args after all, so we assert that it doesn't. Type
   * resolution is then performed.
   */
  private def parseVarArgs(
    syntax: core.Syntax,
    sourceLocation: SourceLocation,
    displayName: String,
    tokens: BufferedIterator[Token],
    scope: SymbolTable): Try[Seq[core.Expression]] = {
      @tailrec
      def parseArgument(toks: BufferedIterator[Token], goalTypes: List[Int], scope: SymbolTable, parsedArgs: Try[Seq[core.Expression]]): Try[Seq[core.Expression]] =
        toks.head match {
          case Token(_, TokenType.Eof, _) =>
            fail(missingInput(syntax, displayName, 0), sourceLocation)
          case Token(_, TokenType.CloseParen, _) => parsedArgs
          case Token(_, TokenType.Reporter, rep: core.Reporter) if goalTypes.head != Syntax.ReporterType && rep.syntax.isInfix =>
            if (parsedArgs.toOption.exists(_.size == syntax.totalDefault))
              parsedArgs
            else
              fail(InvalidVariadicContext, sourceLocation)
          case _ =>
            val newExp = parseArgExpression(syntax, toks, sourceLocation, displayName, goalTypes.head, scope)
            val newGoals = if (goalTypes.tail.nonEmpty) goalTypes.tail else goalTypes
            val newArgs = for {
              args <- parsedArgs
              exp  <- newExp
            } yield args :+ exp
            parseArgument(toks, newGoals, scope, newArgs)
        }
    parseArgument(tokens, syntax.right, scope, Success(Seq()))
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
      syntax:         Syntax,
      tokens:         BufferedIterator[Token],
      sourceLocation: SourceLocation,
      displayName:    String, // app.instruction.displayName
      goalType:       Int,
      scope:          SymbolTable): Try[core.Expression] = {
    parseExpressionInternal(tokens, false, syntax.precedence, goalType, scope) recoverWith {
      case (_: UnexpectedTokenException | _: MissingPrefixException) =>
        fail(missingInput(syntax, displayName, 0), sourceLocation)
    }
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
  private def resolveType(goalType: Int, originalArg: core.Expression, instruction: String, scope: SymbolTable): Try[core.Expression] = {
    // now that we know the type, finish parsing any blocks
    for {
      arg <- originalArg match {
        case block: DelayedBlock => parseDelayedBlock(block, goalType, scope)
        case _                   => Try(originalArg)
      }
      resolved <- cTry(compatible(goalType, arg.reportedType), arg, {
        // remove reference type from message unless it's part of the goalType, confusing to see
        // "expected a variable or a number"
        val displayedReportedType = {
          if ((goalType & Syntax.ReferenceType) == 0 && ((arg.reportedType & ~Syntax.ReferenceType) != 0))
            arg.reportedType & ~Syntax.ReferenceType
          else
            arg.reportedType
        }
        s"$instruction expected this input to be ${core.TypeNames.aName(goalType)}, but got ${core.TypeNames.aName(displayedReportedType)} instead"
      },
      arg)
    } yield resolved
  }

  /**
   * a wrapper around parseExpressionInternal for parsing expressions in non-argument positions
   * (basically only inside parens or reporter blocks).
   *
   * Package protected for unit testing.
   *
   * @param tokens   the input token stream
   * @param variadic whether to treat this expression as possibly variadic
   */
  def parseExpression(tokens: BufferedIterator[Token], variadic: Boolean, goalType: Int, scope: SymbolTable): Try[core.Expression] = {
    parseExpressionInternal(tokens, variadic, MinPrecedence, goalType, scope) recoverWith {
      case e: MissingPrefixException   => fail(MissingInputOnLeft, e.token)
      case e: UnexpectedTokenException => fail(ExpectedReporter, e.token)
    }
  }

  /**
   * parses a parenthesized something. Assumes that the first token in the iterator is an open paren
   *
   * @return Try[(parenthenesized A, Source Location from Opening to Closing Paren)]
   */
  private def parseParenthesized[A](tokens: BufferedIterator[Token], parse: BufferedIterator[Token] => Try[A], filename: String): Try[(A, SourceLocation)] = {
    val openParen = tokens.next()
    assert(openParen.tpe == TokenType.OpenParen)
    for {
      node <- parse(tokens)
      closeParen = tokens.next()
      // if next is an Eof, we complain and point to the open paren.
      node2 <- cTry(closeParen.tpe != TokenType.Eof, node, MissingCloseParen, openParen)
      // if it's anything else other than ), we complain and point to the next token itself.
      node3 <- cTry(closeParen.tpe == TokenType.CloseParen, node, ExpectedCloseParen, closeParen)
    } yield (node3, SourceLocation(openParen.start, closeParen.end, filename))
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
      tokens: BufferedIterator[Token],
      variadic: Boolean,
      precedence: Int,
      goalType: Int,
      scope: SymbolTable): Try[core.Expression] = {
    var token = tokens.head
    val wantAnyLambda = goalType == (Syntax.ReporterType | Syntax.CommandType)
    val wantReporterLambda = wantAnyLambda || goalType == Syntax.ReporterType
    val wantCommandLambda = wantAnyLambda || goalType == Syntax.CommandType
    def finalizeReporterApp(syntax: Syntax, rApp: core.ReporterApp): Try[core.ReporterApp] = {
      // the !variadic check is to prevent "map (f a) ..." from being misparsed.
      if (wantReporterLambda && !variadic && ! wantsSymbolicValue(rApp.reporter) && (wantAnyLambda || syntax.totalDefault > 0))
        Success(expandConciseReporterLambda(rApp, rApp.reporter, scope))
      else // the normal case
        parsePotentiallyVariadicArgumentList(syntax, variadic, rApp, tokens, scope).map(rApp.withArguments _)
    }
    val expr: Try[core.Expression] =
      token.tpe match {
        case TokenType.OpenParen =>
          def parseInnerExp(ts: BufferedIterator[Token]): Try[core.Expression] =
            for {
              exp      <- parseExpression(ts, true, goalType, scope)
              // we also special case an out-of-place command, since this is what the command center does
              // if you leave off a final paren (because of the __done at the end).
              validExp <- cTry(ts.head.tpe != TokenType.Command, exp, MissingCloseParen, token)
            } yield validExp

          parseParenthesized(tokens, parseInnerExp _, token.filename).map {
            case (expr, loc) => expr.changeLocation(loc)
          }
        case TokenType.OpenBracket =>
          delayBlock(token, tokens, scope)
        case TokenType.Reporter | TokenType.Command
            if compatible(goalType, Syntax.SymbolType) =>
          tokens.next()
          val symbol = new core.prim._symbol()
          token.refine(symbol)
          Try(new core.ReporterApp(symbol, token.sourceLocation))
        case TokenType.Literal =>
          tokens.next()
          val coreReporter = new core.prim._const(token.value)
          coreReporter.token = token
          finalizeReporterApp(coreReporter.syntax, new core.ReporterApp(coreReporter, token.sourceLocation))
        case TokenType.Reporter =>
          tokens.next()
          val coreReporter = token.value.asInstanceOf[core.Reporter]
          coreReporter match {
            case (_: core.prim._symbol | _: core.prim._unknownidentifier) if goalType == Syntax.SymbolType =>
              finalizeReporterApp(coreReporter.syntax, new core.ReporterApp(coreReporter, token.sourceLocation))
            case (_: core.prim._symbol | _: core.prim._unknownidentifier) =>
              LetVariableScope(coreReporter, token, scope).map {
                case (letReporter, newScope) =>
                  finalizeReporterApp(letReporter.syntax, new core.ReporterApp(letReporter, token.sourceLocation))
              }.getOrElse {
                fail(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
              }
            // "|| wantReporterLambda" is needed to enable concise syntax for infix reporters, e.g. "map + ..."
            case rep if ! rep.syntax.isInfix || wantReporterLambda =>
              finalizeReporterApp(coreReporter.syntax, new core.ReporterApp(coreReporter, token.sourceLocation))
            // _minus is allowed to be unary (negation) only if it's missing a left argument and
            // in a possibly variadic context (the first thing in a set of parens, basically).
            case _: core.prim._minus if variadic =>
              val r2 = new core.prim._unaryminus
              r2.token = token
              finalizeReporterApp(r2.syntax, new core.ReporterApp(r2, token.sourceLocation))
            case _ =>
              Failure(new MissingPrefixException(token))
          }
        case TokenType.Command if wantCommandLambda =>
          tokens.next()
          expandConciseCommandLambda(token, scope)
        case _ =>
          // here we throw a temporary exception, since we don't know yet what this error means... It
          // generally either means MissingInputOnRight or ExpectedReporter.
          Failure(new UnexpectedTokenException(token))
      }
    expr.flatMap(e => parseMore(e, tokens, precedence, scope))
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
  private def expandConciseCommandLambda(token: Token, scope: SymbolTable): Try[core.ReporterApp] = {
    val coreCommand = token.value.asInstanceOf[core.Command]
    if (! coreCommand.syntax.canBeConcise)
      Failure(new UnexpectedTokenException(token))
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

      Success(new core.ReporterApp(lambda, Seq(commandBlock), token.sourceLocation))
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
  def parseMore(originalExpr: core.Expression, tokens: BufferedIterator[Token], precedence: Int, scope: SymbolTable): Try[core.Expression] =
    if (originalExpr == null) {
      // NOTE: I cannot find any reason why this should be null, but I'm not yet
      // brave enough to remove it - RG 4/28/17
      fail(MissingInputOnLeft, tokens.head)
    } else {
      var done = false

      @tailrec
      def parseMoreRec(tExpr: Try[core.Expression]): Try[core.Expression] = {
        if (tExpr.isFailure)
          tExpr
        else {
          val token = tokens.head
          if (token.tpe == TokenType.Reporter) {
            val coreReporter = token.value.asInstanceOf[core.Reporter]
            val syntax = coreReporter.syntax
            if (syntax.isInfix && (syntax.precedence > precedence ||
              (syntax.isRightAssociative && syntax.precedence == precedence))) {
                tokens.next()
                val (newExpr: Try[core.Expression]) = for {
                  expr        <- tExpr
                  sourceLocation = SourceLocation(expr.start, token.end, token.filename)
                  untypedArgs <- parseArguments(syntax, sourceLocation, coreReporter.displayName, tokens, scope)
                  typedArgs   <- resolveTypes(syntax, expr +: untypedArgs, sourceLocation, coreReporter.displayName, scope)
                } yield {
                  new core.ReporterApp(coreReporter, typedArgs, sourceLocation.copy(end = typedArgs.last.end))
                }
                parseMoreRec(newExpr)
              } else tExpr
          } else tExpr
        }
      }

      parseMoreRec(Success(originalExpr))
    }

  /**
   * packages a block for later parsing. Since blocks can't really be parsed without type
   * information, and since type info isn't available in a simple left-to-right way, we delay the
   * parsing of blocks by packaging up their tokens in a DelayedBlock. This also makes error reports
   * a bit nicer, since we can point out the entire block if something goes wrong. */
  private def delayBlock(openBracket: Token, tokens: BufferedIterator[Token], scope: SymbolTable): Try[DelayedBlock] = {
    import collection.mutable.{ Buffer, UnrolledBuffer }

    // The purpose of the recursion here is to collect all of the tokens until we reach the
    // closing bracket matching the opening bracket at the front of tokens.
    @tailrec
    def collect(tokens:  BufferedIterator[Token],
                acc:     Buffer[Token],
                nesting: Int): Try[Seq[Token]] = {
      val token = tokens.next()
      if (token.tpe == TokenType.OpenBracket)
        collect(tokens, acc += token, nesting + 1)
      else if (token.tpe == TokenType.CloseBracket) {
        if (nesting == 1) Success((acc += token).toSeq)
        else collect(tokens, acc += token, nesting - 1)
      } else if (token.tpe != TokenType.Eof)
        collect(tokens, acc += token, nesting)
      else
        fail(MissingCloseBracket, openBracket)
    }

    collect(tokens, new UnrolledBuffer[Token](), 0).map { collectedBlock =>
      DelayedBlock(openBracket, collectedBlock.tail, scope)
    }
  }

  /**
   * parses a block (i.e., anything in brackets). This deals with reporter blocks (a single
   * expression), command blocks (statements), and literal lists (any number of literals). The
   * initial opening bracket should still be the first token in the tokens in the DelayedBlock.
   */
  private def parseDelayedBlock(block: DelayedBlock, goalType: Int, scope: SymbolTable): Try[core.Expression] = {
    val tokens = block.tokens.tail.iterator.buffered // .tail to drop openBracket

    def statementList(i: BufferedIterator[Token], scope: SymbolTable): Try[(Seq[core.Statement], Token)] = {
      for {
        stmts <- parseStatements(i, scope, TokenType.CloseBracket, { (ts, s) =>
          // if next is an Eof, we complain and point to the open bracket.
          // this should be impossible, since it's a delayed block.
          cTry(ts.head.tpe != TokenType.Eof, true, MissingCloseBracket, block.openBracket).flatMap { _ =>
            parseStatement(ts, false, s)
          }
        })
      } yield (stmts, i.next())
    }

    def reporterApp(i: BufferedIterator[Token], expressionGoal: Int, scope: SymbolTable): Try[(core.ReporterApp, Token)] = {
      val expr = resolveType(Syntax.WildcardType,
        parseExpression(tokens, false, expressionGoal, scope).get, null, scope).get
      val lastToken = tokens.next()
      // should be impossible for delayed block
      cTry(lastToken.tpe != TokenType.Eof, (expr.asInstanceOf[core.ReporterApp], lastToken), MissingCloseBracket, block.openBracket)
        .flatMap { e =>
          cTry(lastToken.tpe == TokenType.CloseBracket, e, ExpectedCloseBracket, lastToken) }
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

<<<<<<< HEAD
    def buildReporterLambda(args: Lambda.Arguments) = {
      val (expr, closeBracket) = reporterApp(tokens, Syntax.WildcardType, block.internalScope)
      val lambda = new core.prim._reporterlambda(args)
      lambda.token = block.openBracket
      new core.ReporterApp(lambda, Seq(expr), SourceLocation(block.openBracket.start, closeBracket.end, block.filename))
    }

    def buildCommandLambda(args: Lambda.Arguments) = {
      val (stmtList, closeBracket) = statementList(tokens, block.internalScope)
      val lambda = new core.prim._commandlambda(args)
      lambda.token = block.openBracket
      val blockArg = commandBlockWithStatements(
        SourceLocation(block.openBracket.start, closeBracket.end, block.filename), stmtList)
      new core.ReporterApp(lambda, Seq(blockArg), SourceLocation(block.openBracket.start, closeBracket.end, block.filename))
=======
    def buildReporterLambda(argTokens: Seq[Token]) = {
      for {
        (expr, closeBracket) <- reporterApp(tokens, Syntax.WildcardType, block.internalScope)
      } yield {
        val lambda = new core.prim._reporterlambda(argTokens.map(_.text.toUpperCase), argTokens, false)
        lambda.token = block.openBracket
        new core.ReporterApp(lambda, Seq(expr), SourceLocation(block.openBracket.start, closeBracket.end, block.filename))
      }
    }

    def buildCommandLambda(argNames: Seq[Token]) = {
      for {
        (stmtList, closeBracket) <- statementList(tokens, block.internalScope)
      } yield {
        val lambda = new core.prim._commandlambda(argNames, false)
        lambda.token = block.openBracket
        val blockArg = commandBlockWithStatements(
          SourceLocation(block.openBracket.start, closeBracket.end, block.filename), stmtList)
        new core.ReporterApp(lambda, Seq(blockArg), SourceLocation(block.openBracket.start, closeBracket.end, block.filename))
      }
>>>>>>> ExpressionParser uses Try instead of throwing exceptions everywhere
    }

    if (compatible(goalType, Syntax.CodeBlockType))
      parseCodeBlock(block, tokens)
    else if (block.isArrowLambda && ! block.isCommand)
      buildReporterLambda(block.asInstanceOf[ArrowLambdaBlock].arguments)
    else if (block.isArrowLambda && block.isCommand)
      buildCommandLambda(block.asInstanceOf[ArrowLambdaBlock].arguments)
    else if (compatible(goalType, Syntax.ReporterBlockType)) {
      for {
        (expr, lastToken) <- reporterApp(tokens, goalType, scope)
      } yield new core.ReporterBlock(expr, SourceLocation(block.openBracket.start, lastToken.end, lastToken.filename))
    } else if (compatible(goalType, Syntax.CommandBlockType)) {
      for {
        (stmtList, lastToken) <- statementList(tokens, scope)
      } yield commandBlockWithStatements(lastToken.sourceLocation.copy(start = block.openBracket.start), stmtList)
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

      Try {
        val (list, closeBracket) =
          new LiteralParser(NullImportHandler).parseLiteralList(block.openBracket, tokens)
        val tmp = new core.prim._const(list)
        tmp.token = new Token("", TokenType.Literal, null)(
          SourceLocation(block.openBracket.start, closeBracket.end, closeBracket.filename))
        new core.ReporterApp(tmp, SourceLocation(block.openBracket.start, closeBracket.end, closeBracket.filename))
      }
    }
    // we weren't actually expecting a block at all!
    else
      fail(s"Expected ${core.TypeNames.aName(goalType)} here, rather than a list or block.", block)
  }

  private def parseCodeBlock(block: DelayedBlock, tokens: BufferedIterator[Token]): Try[core.ReporterApp] = {
    // Because we don't parse the inside of the code block, precisely because we don't want to define
    // legality of code in terms of the standard NetLogo requirements, we have to do a little sanity
    // checking here to make sure that at the very least, parenthesis and brackets are matched up
    // without being mixed and matched.  FD 8/19/2015

    val tokens = block match {
      case alb: ArrowLambdaBlock => alb.allTokens
      case adl: AmbiguousDelayedBlock => adl.tokens
    }

    @tailrec
    def checkedTokens(remaining: Seq[Token], stack: Seq[Token] = Seq()): Try[Seq[Token]] = {
      if(remaining.isEmpty) {
        if(!stack.isEmpty && stack.head.tpe == TokenType.OpenParen && tokens.length > 1) {
          fail("Expected close paren here", block.tokens(tokens.length - 2)) // the last token is EOF, we want the close bracket
        } else
        // Drop two because of the CloseRightBracket, EOF
        Success(tokens.tail.dropRight(2))
      } else {
        remaining.head.tpe match {
          case (TokenType.OpenBracket | TokenType.OpenParen) =>
            checkedTokens(remaining.tail, remaining.head +: stack)
          case TokenType.CloseBracket =>
            if(!stack.isEmpty && stack.head.tpe == TokenType.OpenParen) {
              fail("Expected close paren before close bracket here", remaining.head)
            }
            if(stack.isEmpty || stack.head.tpe != TokenType.OpenBracket) {
              fail("Closing bracket has no matching open bracket here", remaining.head)
            }
            checkedTokens(remaining.tail, stack.tail)
          case TokenType.CloseParen =>
            if(!stack.isEmpty && stack.head.tpe == TokenType.OpenBracket) {
              fail("Expected close bracket before close paren here", remaining.head)
            }
            if(stack.isEmpty || stack.head.tpe != TokenType.OpenParen) {
              fail("Closing paren has no matching open paren here", remaining.head)
            }
            checkedTokens(remaining.tail, stack.tail)
          case _ => checkedTokens(remaining.tail, stack)
        }
      }
    }

    for {
      toks <- checkedTokens(tokens.dropRight(2))
    } yield {
      val tmp = new core.prim._constcodeblock(toks)
      tmp.token = tokens.head
      new core.ReporterApp(tmp, SourceLocation(tokens.head.start, block.end, tokens.head.filename))
    }
  }

  private class MissingPrefixException(val token: Token) extends Exception
  private class UnexpectedTokenException(val token: Token) extends Exception

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
  private val MissingCloseBracket = "No closing bracket for this open bracket."
  private val MissingCloseParen = "No closing parenthesis for this open parenthesis."
  private val MissingInputOnLeft = "Missing input on the left."

}
