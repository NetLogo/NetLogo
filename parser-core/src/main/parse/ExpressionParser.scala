// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ prim, FrontEndProcedure, Fail, I18N, SourceLocation, Syntax, Token, TokenType },
    prim.Lambda,
    Fail.{ cTry, fail },
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

  private def isEnd(g: SyntaxGroup, end: TokenType): Boolean = {
    g match {
      case Atom(a) => a.tpe == end
      case _ => false
    }
  }

  type ParseResult[A] = Try[(A, Seq[SyntaxGroup])]

  /**
   * parses a procedure. Procedures are a bunch of statements (not a block of statements, that's
   * something else), and so are parsed as such. */
  def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token], scope: SymbolTable): core.ProcedureDefinition = {
    val buffered = tokens.buffered
    val head = buffered.head
    val statementList =
      groupSyntax(buffered).flatMap { groupedTokens =>
        parseStatements(groupedTokens, scope, TokenType.Eof, parseStatement(_, false, _)).map(_._1)
      }.get

    // tryStatementList.failed.foreach(_.printStackTrace())
    val stmts = new core.Statements(head.filename, statementList, false)
    val end =
      if (head.end < Int.MaxValue) head.start
      else stmts.end
    new core.ProcedureDefinition(procedureDeclaration, stmts, end)
  }

  def groupSyntax(tokens: BufferedIterator[Token]): Try[Seq[SyntaxGroup]] = {
    val tokenTpeMatch = Map[TokenType, TokenType](
      TokenType.CloseBracket -> TokenType.OpenBracket,
      TokenType.CloseParen -> TokenType.OpenParen)
    def delimName(tpe: TokenType) =
      if (tpe == TokenType.OpenParen || tpe == TokenType.CloseParen) "parenthesis" else "bracket"
    @tailrec
    def groupRec(acc: Seq[SyntaxGroup], stack: List[Token], groupStack: List[List[SyntaxGroup]]): Try[Seq[SyntaxGroup]] = {
      if (! tokens.hasNext) Success(acc)
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

  private def parseStatements(
    groups: Seq[SyntaxGroup],
    scope: SymbolTable,
    terminator: TokenType,
    f: (Seq[SyntaxGroup], SymbolTable) => Try[(core.Statement, Seq[SyntaxGroup], SymbolTable)]): ParseResult[Seq[core.Statement]] = {
      val b = Buffer[core.Statement]()
      var remainingGroups = groups
      var activeScope = scope
      var badStmt = Option.empty[Failure[(Seq[core.Statement], Seq[SyntaxGroup])]]
      while (badStmt.isEmpty && remainingGroups.nonEmpty && ! isEnd(remainingGroups.head, terminator)) {
        f(remainingGroups, activeScope) match {
          case Success((stmt, newGroups, newScope)) =>
            remainingGroups = newGroups
            activeScope = newScope
            b += stmt
          case Failure(ex) =>
            badStmt = Some(Failure(ex))
        }
      }
      badStmt.getOrElse(Try((b.toSeq, remainingGroups)))
  }

  /**
   * parses a statement.
   *
   * @return the parsed statement and the symbol table resulting from any added scope
   */
  private def parseStatement(groups: Seq[SyntaxGroup], variadic: Boolean, scope: SymbolTable): Try[(core.Statement, Seq[SyntaxGroup], SymbolTable)] = {
    groups.head match {
      case pg@ParenGroup(subgroups, open, close) =>
        parseStatement(subgroups, true, scope).flatMap {
          case (stmt, remainingGroups, newScope) =>
            if (remainingGroups.nonEmpty)
              fail(ExpectedCloseParen,
                SourceLocation(remainingGroups.head.start.start, remainingGroups.head.start.end, remainingGroups.head.start.filename))
            else
              Success((stmt.changeLocation(pg.location), groups.tail, newScope))
        }
      case Atom(token) if token.tpe == TokenType.Command =>
        val coreCommand = token.value.asInstanceOf[core.Command]
        val nameToken = groups.tail.headOption match {
          case Some(Atom(t)) if t.tpe != TokenType.Eof => Some(t)
          case _ => None
        }

        val (stmt, newScope, remainingGroups) =
          LetScope(coreCommand, nameToken, scope).map {
            case (letCommand, newScope) =>
              (new core.Statement(letCommand, token.sourceLocation), newScope, groups.tail)
          }.getOrElse((new core.Statement(coreCommand, token.sourceLocation), scope, groups.tail))

        for {
          (arguments, groupsAfterArgumentList) <-
            parsePotentiallyVariadicArgumentList(coreCommand.syntax, variadic, stmt, remainingGroups, newScope)
        } yield (stmt.withArguments(arguments), groupsAfterArgumentList, newScope)
      case other =>
        val token = other.allTokens.head
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
    groups: Seq[SyntaxGroup],
    scope: SymbolTable): ParseResult[Seq[core.Expression]] = {
    val untypedArgs =
      if (variadicContext && syntax.isVariadic) parseVarArgs(syntax, app.sourceLocation, app.instruction.displayName, groups, scope)
      else                                      parseArguments(syntax, app.sourceLocation, app.instruction.displayName, groups, scope)

      for {
        (parsedArgs, remainingGroups) <- untypedArgs
        typedExpressions              <- resolveTypes(syntax, parsedArgs, app.sourceLocation, app.instruction.displayName, scope)
      } yield (typedExpressions, remainingGroups)
  }

  private def traverse[A](elems: Seq[Try[A]]): Try[Seq[A]] = {
    elems.foldLeft(Try(Seq.empty[A])) {
      case (acc, Success(a))    => acc.map { as => as :+ a }
      case (acc: Failure[Seq[A]], f: Failure[A]) => acc
      case (acc, Failure(e)) => Failure(e)
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
      val formal1 = index
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

  var timesCalled = 0
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
    groups: Seq[SyntaxGroup],
    scope: SymbolTable): ParseResult[Seq[core.Expression]] = {
    import syntax.{ right, takesOptionalCommandBlock }
    val parsedArgs =
      (0 until syntax.rightDefault).foldLeft(Try((List.empty[core.Expression], groups))) { (t: Try[(List[core.Expression], Seq[SyntaxGroup])], i: Int) =>
        t.flatMap {
          case (acc, gs) =>
            parseArgExpression(syntax, gs, sourceLocation, displayName, right(i min (right.size - 1)), scope).map {
              case (exp, restOfGroups) => (exp :: acc, restOfGroups)
            }
        }
      }
      .map {
        case (args, restOfGroups) => (args.reverse, restOfGroups)
      }

    if (! takesOptionalCommandBlock) parsedArgs
    else {
      parsedArgs.flatMap {
        case (args, newGroups) =>
          newGroups.headOption match {
            case Some(b@BracketGroup(_, _, _)) =>
              for {
                (cmdBlock, restGroups) <- parseArgExpression(syntax, Seq(b), sourceLocation, displayName, right.last, scope)
              } yield (args :+ cmdBlock, newGroups.tail)
            case other =>
              Success((args :+ {
                // synthetic block so later phases of compilation have consistent number of arguments
                val file = sourceLocation.filename
                val location = args.lastOption.map(_.end) getOrElse sourceLocation.end
                new core.CommandBlock(new core.Statements(file), SourceLocation(location, location, file), true)
              }, newGroups))
          }
      }
    }
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). We'll continue parsing expressions until we encounter an error or
   * a lower precedence binary reporter. In the latter case, it turns out that this app
   * can't have a non-default number of args after all, so we assert that it doesn't. Type
   * resolution is then performed.
   */
  private def parseVarArgs(
    syntax: core.Syntax,
    sourceLocation: SourceLocation,
    displayName: String,
    groups: Seq[SyntaxGroup],
    scope: SymbolTable): Try[(Seq[core.Expression], Seq[SyntaxGroup])] = {
      def parseArgument(groups: Seq[SyntaxGroup], goalTypes: List[Int], scope: SymbolTable, parsedArgs: Try[Seq[core.Expression]]): Try[(Seq[core.Expression], Seq[SyntaxGroup])] =
        groups.headOption match {
          // corresponds to end of parentheses
          case None                => parsedArgs.map(as => (as, Seq.empty[SyntaxGroup]))
          // NOTE: We used to check here whether we were at Eof, but since we group things that's no longer necessary
          case Some(Atom(Token(_, TokenType.Reporter, rep: core.Reporter))) if goalTypes.head != Syntax.ReporterType && rep.syntax.isInfix  =>
            if (parsedArgs.toOption.exists(_.size == syntax.totalDefault))
              parsedArgs.map(as => (as, groups.tail))
            else
              fail(InvalidVariadicContext, sourceLocation)
          case other =>
            parsedArgs.flatMap { args =>
              parseArgExpression(syntax, groups, sourceLocation, displayName, goalTypes.head, scope).flatMap {
                case (newExp: core.Expression, remainingGroups: Seq[SyntaxGroup]) =>
                  val newGoals = if (goalTypes.tail.nonEmpty) goalTypes.tail else goalTypes
                  parseArgument(remainingGroups, newGoals, scope, Success(args :+ newExp))
              }
            }
        }
    parseArgument(groups, syntax.right, scope, Success(Seq()))
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
      groups:         Seq[SyntaxGroup],
      sourceLocation: SourceLocation,
      displayName:    String, // app.instruction.displayName
      goalType:       Int,
      scope:          SymbolTable): ParseResult[core.Expression] = {
    parseExpressionInternal(groups, false, syntax.precedence, goalType, scope) recoverWith {
      case e@(_: UnexpectedTokenException | _: MissingPrefixException) =>
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
  def parseExpression(groups: Seq[SyntaxGroup], variadic: Boolean, goalType: Int, scope: SymbolTable): ParseResult[core.Expression] = {
    parseExpressionInternal(groups, variadic, MinPrecedence, goalType, scope) recoverWith {
      case e: MissingPrefixException   => fail(MissingInputOnLeft, e.token)
      case e: UnexpectedTokenException => fail(ExpectedReporter, e.token)
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
      variadic: Boolean,
      precedence: Int,
      goalType: Int,
      scope: SymbolTable): ParseResult[core.Expression] = {
    val wantAnyLambda = goalType == (Syntax.ReporterType | Syntax.CommandType)
    val wantReporterLambda = wantAnyLambda || goalType == Syntax.ReporterType
    val wantCommandLambda = wantAnyLambda || goalType == Syntax.CommandType
    def finalizeReporterApp(syntax: Syntax, rApp: core.ReporterApp): ParseResult[core.ReporterApp] = {
      val remainingGroups = groups.tail
      // the !variadic check is to prevent "map (f a) ..." from being misparsed.
      if (wantReporterLambda && !variadic && ! wantsSymbolicValue(rApp.reporter) && (wantAnyLambda || syntax.totalDefault > 0))
        Success((expandConciseReporterLambda(rApp, rApp.reporter, scope), remainingGroups))
      else // the normal case
        parsePotentiallyVariadicArgumentList(syntax, variadic, rApp, remainingGroups, scope).map {
          case (e, remainingGroups2) => (rApp.withArguments(e), remainingGroups2)
        }
    }
    val expr: ParseResult[core.Expression] =
      groups.head match {
        case ParenGroup(inner, open, close) =>
          // TOOD: Not sure how this works with SyntaxGroups
          // we also special case an out-of-place command, since this is what the command center does
          // if you leave off a final paren (because of the __done at the end).
          parseExpression(inner, true, goalType, scope).flatMap {
            case (arg, remainingGroups) =>
              if (remainingGroups.isEmpty) Success((arg, remainingGroups))
              else fail(ExpectedCloseParen, remainingGroups.head.start)
          }
        case group: BracketGroup => delayBlock(group, scope).map(block => (block, groups.tail))
        case Atom(token) =>
          token.tpe match {
            case TokenType.Reporter | TokenType.Command
                if compatible(goalType, Syntax.SymbolType) =>
              val symbol = new core.prim._symbol()
              token.refine(symbol)
              Try((new core.ReporterApp(symbol, token.sourceLocation), groups.tail))
            case TokenType.Literal =>
              val coreReporter = new core.prim._const(token.value)
              coreReporter.token = token
              finalizeReporterApp(coreReporter.syntax, new core.ReporterApp(coreReporter, token.sourceLocation))
            case TokenType.Reporter =>
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
              expandConciseCommandLambda(token, scope).map(lambda => (lambda, groups.tail))
            case _ =>
              // here we throw a temporary exception, since we don't know yet what this error means... It
              // generally either means MissingInputOnRight or ExpectedReporter.
              Failure(new UnexpectedTokenException(token))
          }
      }
    expr.flatMap {
      case (e, groups) => parseMore(e, groups, precedence, scope)
    }
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
  def parseMore(originalExpr: core.Expression, groups: Seq[SyntaxGroup], precedence: Int, scope: SymbolTable): ParseResult[core.Expression] = {
      def parseMoreRec(tExpr: ParseResult[core.Expression]): ParseResult[core.Expression] = {
        if (tExpr.isFailure)
          tExpr
        else {
          tExpr.flatMap {
            case (expr, groups) =>
              groups.headOption match {
                case Some(Atom(token@Token(_, TokenType.Reporter, coreReporter: core.Reporter))) =>
                  val syntax = coreReporter.syntax
                  if (syntax.isInfix && (syntax.precedence > precedence ||
                    (syntax.isRightAssociative && syntax.precedence == precedence))) {
                      val sourceLocation = SourceLocation(expr.start, token.end, token.filename)
                      val newExpr = parseArguments(syntax, sourceLocation, coreReporter.displayName, groups.tail, scope).flatMap {
                        case (untypedArgs, remainingGroups) =>
                          resolveTypes(syntax, expr +: untypedArgs, sourceLocation, coreReporter.displayName, scope).map {
                            typedArgs =>
                              (new core.ReporterApp(coreReporter, typedArgs, sourceLocation.copy(end = typedArgs.last.end)), remainingGroups)
                          }
                      }
                      parseMoreRec(newExpr)
                    } else tExpr
                 case _ =>  tExpr
              }
          }
        }
      }

      parseMoreRec(Success((originalExpr, groups)))
    }

  /**
   * packages a block for later parsing. Since blocks can't really be parsed without type
   * information, and since type info isn't available in a simple left-to-right way, we delay the
   * parsing of blocks by packaging up their tokens in a DelayedBlock. This also makes error reports
   * a bit nicer, since we can point out the entire block if something goes wrong. */
  private def delayBlock(group: BracketGroup, scope: SymbolTable): Try[DelayedBlock] = {
    Success(DelayedBlock(group, scope))
  }

  /**
   * parses a block (i.e., anything in brackets). This deals with reporter blocks (a single
   * expression), command blocks (statements), and literal lists (any number of literals). The
   * initial opening bracket should still be the first token in the tokens in the DelayedBlock.
   */
  private def parseDelayedBlock(block: DelayedBlock, goalType: Int, scope: SymbolTable): Try[core.Expression] = {
    def statementList(block: DelayedBlock, scope: SymbolTable): Try[Seq[core.Statement]] = {
      parseStatements(block.bodyGroups, block.internalScope, TokenType.CloseBracket, { (groups, s) =>
        parseStatement(groups, false, s)
      }).flatMap {
        case (stmts, rest) =>
          // if we haven't gotten everything, we complain
          cTry(rest.isEmpty, stmts, ExpectedCommand, block.openBracket)
      }
    }

    def reporterApp(block: DelayedBlock, expressionGoal: Int, scope: SymbolTable): Try[core.ReporterApp] = {
      parseExpression(block.bodyGroups, false, expressionGoal, scope).flatMap {
        case (exp, remainingGroups) =>
          resolveType(Syntax.WildcardType, exp, null, scope).flatMap {
            case (expr: core.ReporterApp) =>
              cTry(remainingGroups.isEmpty, expr, ExpectedCloseBracket, block.openBracket)
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

      Try {
        // this token-iterator may need adjustment...
        val (list, closeBracket) =
          new LiteralParser(NullImportHandler).parseLiteralList(block.openBracket, block.group.allTokens.drop(1).iterator)
        val tmp = new core.prim._const(list)
        tmp.token = new Token("", TokenType.Literal, null)(block.group.location)
        new core.ReporterApp(tmp, block.group.location)
      }
    }
    // we weren't actually expecting a block at all!
    else
      fail(s"Expected ${core.TypeNames.aName(goalType)} here, rather than a list or block.", block)
  }

  private def parseCodeBlock(block: DelayedBlock): Try[core.ReporterApp] = {
    // Because we don't parse the inside of the code block, precisely because we don't want to define
    // legality of code in terms of the standard NetLogo requirements, we simply gather the tokens and leave
    // it alone. FD 8/19/2015, RG 5/2/2017

    val tokens = block match {
      case alb: ArrowLambdaBlock => alb.allTokens
      case adl: AmbiguousDelayedBlock => adl.tokens
    }

    Try {
      val tmp = new core.prim._constcodeblock(tokens.tail.dropRight(2))
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
  // private val MissingCloseBracket = "No closing bracket for this open bracket."
  // private val MissingCloseParen = "No closing parenthesis for this open parenthesis."
  private val MissingInputOnLeft = "Missing input on the left."

}
