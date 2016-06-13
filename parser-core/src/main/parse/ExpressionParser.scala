// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{FrontEndProcedure, Fail, I18N, StructureDeclarations, Syntax, Token, TokenType},
    Fail.{ cAssert, exception },
    Syntax.compatible

import scala.annotation.tailrec

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
  def apply(procedureDeclaration: FrontEndProcedure, tokens: Iterator[Token]): core.ProcedureDefinition = {
    val buffered = tokens.buffered
    val stmts = new core.Statements(buffered.head.filename)
    while (buffered.head.tpe != TokenType.Eof) {
      stmts.addStatement(parseStatement(buffered, false))
    }
    val pd = new core.ProcedureDefinition(procedureDeclaration, stmts)
    if (buffered.head.end < Int.MaxValue) {
      pd.end = buffered.head.start
    }
    pd
  }

  /**
   * parses a statement.
   */
  private def parseStatement(tokens: BufferedIterator[Token], variadic: Boolean): core.Statement = {
    val token = tokens.next()
    token.tpe match {
      case TokenType.OpenParen =>
        val openParen = token
        val stmt = parseStatement(tokens, true)
        // if next is an Eof, we complain and point to the open paren.
        cAssert(tokens.head.tpe != TokenType.Eof, MissingCloseParen, openParen)
        val closeParen = tokens.next()
        // if next is anything else other than ), we complain and point to the next token itself.
        cAssert(closeParen.tpe == TokenType.CloseParen, ExpectedCloseParen, closeParen)
        // now tidy up the origin to reflect the parens.
        stmt.start = openParen.start
        stmt.end = token.end
        stmt
      case TokenType.Command =>
        val coreCommand = token.value.asInstanceOf[core.Command]
        val stmt =
          new core.Statement(coreCommand,
            token.start, token.end, token.filename)
        if (variadic && coreCommand.syntax.isVariadic)
          parseVarArgs(coreCommand.syntax, stmt, tokens)
        else
          parseArguments(coreCommand.syntax, stmt, tokens)
        stmt
      case TokenType.Reporter if token.value.isInstanceOf[core.prim._symbol] =>
        exception(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
      case _ =>
        exception(ExpectedCommand, token)
    }
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). The number is dictated by the syntax of the head instruction of the application
   * whose arguments we're parsing. We expect exactly that number of args.  Type resolution is then
   * performed.
   */
  private def parseArguments(syntax: Syntax, app: core.Application, tokens: BufferedIterator[Token]) {
    val right = syntax.right
    val optional = syntax.takesOptionalCommandBlock
    for(i <- 0 until syntax.rightDefault) {
      val arg = parseArgExpression(syntax, tokens, app, right(i min (right.size - 1)))
      app.addArgument(arg)
      app.end = arg.end
    }
    if (optional)
      if (tokens.head.tpe == TokenType.OpenBracket) {
        val arg = parseArgExpression(syntax, tokens, app, right.last)
        app.addArgument(arg)
        app.end = arg.end
      }
      else {
        // synthesize an empty block so that later phases of compilation will be dealing with a
        // consistent number of arguments - ST 3/4/08
        val file = tokens.head.filename
        app.addArgument(new core.CommandBlock(new core.Statements(file), app.end, app.end, file))
      }
    // check all types
    resolveTypes(syntax, app)
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). We'll continue parsing expressions until we encounter a closing parenthesis, an
   * error, or a lower precedence binary reporter. In the last case, it turns out that this app
   * can't have a non-default number of args after all, so we assert that it doesn't. Type
   * resolution is then performed.
   */
  private def parseVarArgs(syntax: Syntax, app: core.Application, tokens: BufferedIterator[Token]) {
    var done = false
    var token = tokens.head
    var argNumber = 0
    val right = syntax.right
    def goalType = right(argNumber min (right.size - 1))
    while (!done) {
      if (token.tpe == TokenType.CloseParen)
        done = true
      else if (token.tpe == TokenType.Reporter &&
               goalType != Syntax.ReporterTaskType &&
               token.value.asInstanceOf[core.Reporter].syntax.isInfix) {
        // we can be confident that any infix op still in tokens
        // at this point is lower precedence, or we would already
        // have consumed it. so if we have a non-default number of
        // args, this is definitely illegal.
        cAssert(app.args.size == syntax.totalDefault, InvalidVariadicContext, app)
        done = true
      }
      // note: if it's a reporter, it must be the beginning
      // of the next arg.
      else {
        val arg = parseArgExpression(syntax, tokens, app, goalType)
        app.addArgument(arg)
        app.end = arg.end
        token = tokens.head
      }
      argNumber += 1
    }
    // check all types
    resolveTypes(syntax, app)
  }

  /**
   * this is used for generating an error message when some arguments are found to be missing
   */
  private def missingInput(syntax: Syntax, displayName: String, right: Boolean): String = {
    val rightArgs = syntax.right.map(core.TypeNames.aName(_).replaceFirst("anything", "any input"))
    val left = syntax.left
    val result =
      if (right && syntax.isVariadic && syntax.minimum == 0)
        displayName + " expected " + syntax.rightDefault +
          " input" + (if(syntax.rightDefault > 1) "s" else "") +
          " on the right or any number of inputs when surrounded by parentheses"
      else
        displayName + " expected " +
          (if (syntax.isVariadic) "at least " else "") +
          (if (right)
             syntax.rightDefault + " input" + (if (syntax.rightDefault > 1) "s"
                                               else "") +
             (if (syntax.isInfix) " on the right" else "")
           else
             core.TypeNames.aName(left) + " on the left.")
    if (!right)
      result
    else if (rightArgs.forall(_ == "any input"))
      result + "."
    else if (rightArgs.size == 1)
      result + ", " + rightArgs.mkString + "."
    else
      result + ", " + rightArgs.toList.dropRight(1).mkString(", ") + " and " + rightArgs.last + "."
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
  private def resolveTypes(syntax: Syntax, app: core.Application) {
    var actual1 = 0
    // first look at left arg, if any
    if (syntax.isInfix) {
      val tpe = syntax.left
      // this shouldn't really be possible here...
      cAssert(app.args.size >= 1, missingInput(syntax, app.instruction.displayName, false), app)
      app.replaceArg(0, resolveType(tpe, app.args(0), app.instruction.displayName))
      // the first right arg is the second arg.
      actual1 = 1
    }
    // look at right args from left-to-right...
    var formal1 = 0
    val types = syntax.right
    while (formal1 < types.length && !compatible(Syntax.RepeatableType, types(formal1))) {
      if (formal1 == types.length - 1 && app.args.size == types.length - 1 &&
          compatible(Syntax.OptionalType, types(formal1)))
        return
      cAssert(app.args.size > actual1, missingInput(syntax, app.instruction.displayName, true), app)
      app.replaceArg(actual1,
        resolveType(types(formal1), app.args(actual1),
          app.instruction.displayName))
      formal1 += 1
      actual1 += 1
    }
    if (formal1 < types.length) {
      // then we encountered a repeatable arg, so we look at right args from right-to-left...
      var actual2 = app.args.size - 1
      var formal2 = types.length - 1
      while (formal2 >= 0 && !compatible(Syntax.RepeatableType, types(formal2))) {
        cAssert(app.args.size > actual2 && actual2 > -1, missingInput(syntax, app.instruction.displayName, true), app)
        app.replaceArg(actual2,
          resolveType(types(formal2), app.args(actual2),
            app.instruction.displayName))
        formal2 -= 1
        actual2 -= 1
      }
      // now we check any repeatable args...
      while (actual1 <= actual2) {
        app.replaceArg(actual1,
          resolveType(types(formal1), app.args(actual1),
            app.instruction.displayName))
        actual1 += 1
      }
    }
  }

  /**
   * resolves the type of an expression. We call this "resolution" instead of "checking" because
   * sometimes the expression needs further parsing or processing depending on its context and
   * expected type. For example, delayed blocks need to be parsed here based on what they're
   * expected to be. The caller should replace the expr it passed in with the one returned,
   * as it may be different.
   */
  private def resolveType(goalType: Int, originalArg: core.Expression, instruction: String): core.Expression = {
    // now that we know the type, finish parsing any blocks
    val arg = originalArg match {
      case block: DelayedBlock => parseDelayedBlock(block, goalType)
      case _ => originalArg
    }
    cAssert(compatible(goalType, arg.reportedType), {
      // remove reference type from message unless it's part of the goalType, confusing to see
      // "expected a variable or a number"
      val displayedReportedType =
        if ((goalType & Syntax.ReferenceType) == 0 &&
          ((arg.reportedType & ~Syntax.ReferenceType) != 0))
          arg.reportedType & ~Syntax.ReferenceType
        else
          arg.reportedType
      s"$instruction expected this input to be ${core.TypeNames.aName(goalType)}, but got ${core.TypeNames.aName(displayedReportedType)} instead"
    },
    arg)
    arg
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
  def parseExpression(tokens: BufferedIterator[Token], variadic: Boolean, goalType: Int): core.Expression = {
    try
      parseExpressionInternal(tokens, variadic, MinPrecedence, goalType)
    catch {
      case e: MissingPrefixException => exception(MissingInputOnLeft, e.token)
      case e: UnexpectedTokenException => exception(ExpectedReporter, e.token)
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
      syntax: Syntax,
      tokens: BufferedIterator[Token],
      app: core.Application,
      goalType: Int): core.Expression = {
    try
      parseExpressionInternal(tokens, false, syntax.precedence, goalType)
    catch {
      case _: MissingPrefixException | _: UnexpectedTokenException =>
        exception(missingInput(syntax, app.instruction.displayName, true), app)
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
      tokens: BufferedIterator[Token],
      variadic: Boolean,
      precedence: Int,
      goalType: Int): core.Expression = {
    var token = tokens.head
    val wantAnyTask = goalType == (Syntax.ReporterTaskType | Syntax.CommandTaskType)
    val wantReporterTask = wantAnyTask || goalType == Syntax.ReporterTaskType
    val wantCommandTask = wantAnyTask || goalType == Syntax.CommandTaskType
    val expr: core.Expression =
      token.tpe match {
        case TokenType.OpenParen =>
          val openParen = token
          tokens.next()
          val expr = parseExpression(tokens, true, goalType)
          token = tokens.head
          // if next is an Eof, we complain and point to the open paren.
          cAssert(tokens.head.tpe != TokenType.Eof, MissingCloseParen, openParen)
          // we also special case an out-of-place command, since this is what the command center does
          // if you leave off a final paren (because of the __done at the end).
          cAssert(token.tpe != TokenType.Command, MissingCloseParen, openParen)
          // if it's anything else other than ), we complain and point to the next token itself.
          cAssert(token.tpe == TokenType.CloseParen, ExpectedCloseParen, token)
          tokens.next()
          // now tidy up the origin to reflect the parens.
          expr.start = openParen.start
          expr.end = token.end
          expr
        case TokenType.OpenBracket =>
          delayBlock(token, tokens)
        case TokenType.Reporter | TokenType.Command
          if compatible(goalType, Syntax.SymbolType) =>
          tokens.next()
          val symbol = new core.prim._symbol()
          token.refine(symbol)
          new core.ReporterApp(symbol, token.start, token.end, token.filename)
        case TokenType.Reporter | TokenType.Literal =>
          tokens.next()
          val (syntax, rApp) = token.tpe match {
            case TokenType.Literal =>
              val coreReporter = new core.prim._const(token.value)
              coreReporter.token = token
              (coreReporter.syntax,
                new core.ReporterApp(coreReporter, token.start, token.end, token.filename))
            case TokenType.Reporter =>
              val coreReporter = token.value.asInstanceOf[core.Reporter]
              if (coreReporter.isInstanceOf[core.prim._symbol]) {
                if (goalType == Syntax.SymbolType) {
                  (coreReporter.syntax, new core.ReporterApp(coreReporter, token.start, token.end, token.filename))
                } else {
                  exception(I18N.errors.getN("compiler.LetVariable.notDefined", token.text.toUpperCase), token)
                }
              }
              // the "|| wantReporterTask" is needed or the concise syntax wouldn't work for infix
              // reporters, e.g. "map + ..."
              if (!coreReporter.syntax.isInfix || wantReporterTask)
                (coreReporter.syntax,
                  new core.ReporterApp(coreReporter, token.start, token.end, token.filename))
              else {
                // this is a bit of a hack, but it's not terrible.  _minus is allowed to be unary
                // (negation) but only if it's missing a left argument and is in a possibly variadic
                // context (the first thing in a set of parens, basically).
                if (!coreReporter.isInstanceOf[core.prim._minus] || !variadic)
                  throw new MissingPrefixException(token)
                val unaryMinusSyntax =
                  Syntax.reporterSyntax(
                    right = List(Syntax.NumberType),
                    ret = Syntax.NumberType)
                val r2 = new core.prim._unaryminus
                r2.token = token
                (r2.syntax,
                  new core.ReporterApp(r2, token.start, token.end, token.filename))
              }
            case _ =>
              sys.error("unexpected token type: " + token.tpe)
          }
          // the !variadic check is to prevent "map (f a) ..." from being misparsed.
          if (wantReporterTask && !variadic && (wantAnyTask || syntax.totalDefault > 0))
            expandConciseReporterTask(rApp, rApp.reporter)
          // the normal case
          else {
            if (variadic && syntax.isVariadic)
              parseVarArgs(syntax, rApp, tokens)
            else
              parseArguments(syntax, rApp, tokens)
            rApp
          }
        case TokenType.Command if wantCommandTask =>
          tokens.next()
          expandConciseCommandTask(token)
        case _ =>
          // here we throw a temporary exception, since we don't know yet what this error means... It
          // generally either means MissingInputOnRight or ExpectedReporter.
          throw new UnexpectedTokenException(token)
      }
    parseMore(expr, tokens, precedence)
  }

  /**
    * handle the case of the concise task syntax, where I can write e.g. "map + ..." instead
    * of "map [?1 + ?2] ...".  for the task primitive itself we allow this even for literals
    *  and nullary reporters, for the other primitives like map we require the reporter to
    *  take at least one input (since otherwise a simple "map f xs" wouldn't evaluate f).
    */
  private def expandConciseReporterTask(rApp: core.ReporterApp, reporter: core.Reporter): core.ReporterApp = {
    val task = new core.prim._reportertask
    task.token = reporter.token
    val taskApp =
      new core.ReporterApp(task,
        reporter.token.start, reporter.token.end, reporter.token.filename)
    taskApp.addArgument(rApp)
    for(argNumber <- 1 to reporter.syntax.totalDefault) {
      val lv = new core.prim._taskvariable(argNumber)
      lv.token = reporter.token
      rApp.addArgument(
        new core.ReporterApp(lv,
          reporter.token.start, reporter.token.end, reporter.token.filename))
    }
    taskApp
  }

  // expand e.g. "foreach xs print" -> "foreach xs [ print ? ]"
  private def expandConciseCommandTask(token: Token): core.ReporterApp = {
    val coreCommand = token.value.asInstanceOf[core.Command]
    val stmt = new core.Statement(coreCommand,
      token.start, token.end, token.filename)
    val task = new core.prim._commandtask
    task.token = token
    for(argNumber <- 1 to coreCommand.syntax.totalDefault) {
      val lv = new core.prim._taskvariable(argNumber)
      lv.token = token
      stmt.addArgument(new core.ReporterApp(lv,
        token.start, token.end, token.filename))
    }
    if (coreCommand.syntax.takesOptionalCommandBlock)
      // synthesize an empty block so that later phases of compilation will be dealing with a
      // consistent number of arguments - ST 3/4/08
      stmt.addArgument(
        new core.CommandBlock(
          new core.Statements(token.filename),
          token.start, token.end, token.filename))
    val stmts = new core.Statements(token.filename)
    stmts.addStatement(stmt)
    val rapp =
      new core.ReporterApp(task,
        token.start, token.end, token.filename)
    rapp.addArgument(
      new core.CommandBlock(stmts,
        token.start, token.end, token.filename))
    rapp
  }

  /**
   * possibly parses the rest of the current expression. Expressions are parsed in a slightly
   * strange way. First we parse an expr, then we look ahead to see if the next token is an infix
   * operator. If so, we use what we've seen so far as its first arg, and then parse its other
   * arguments. Then we repeat. The result of all of this is the actual expr.
   */
  def parseMore(originalExpr: core.Expression, tokens: BufferedIterator[Token], precedence: Int): core.Expression = {
    var expr = originalExpr
    var done = false
    while (!done) {
      val token = tokens.head
      if (token.tpe == TokenType.Reporter) {
        val coreReporter = token.value.asInstanceOf[core.Reporter]
        val syntax = coreReporter.syntax
        if (syntax.isInfix && (syntax.precedence > precedence ||
                                (syntax.isRightAssociative && syntax.precedence == precedence))) {
          tokens.next()
          // note: this actually shouldn't be possible here, because this should never be called
          // with null expr, but better safe than sorry...
          cAssert(expr != null, MissingInputOnLeft, token)
          val tmp = new core.ReporterApp(coreReporter,
            expr.start, token.end, token.filename)
          tmp.addArgument(expr)
          parseArguments(syntax, tmp, tokens)
          expr = tmp
        }
        else done = true
      }
      else done = true
    }
    expr
  }

  /**
   * packages a block for later parsing. Since blocks can't really be parsed without type
   * information, and since type info isn't available in a simple left-to-right way, we delay the
   * parsing of blocks by packaging up their tokens in a DelayedBlock. This also makes error reports
   * a bit nicer, since we can point out the entire block if something goes wrong. */
  private def delayBlock(openBracket: Token, tokens: BufferedIterator[Token]): DelayedBlock = {
    // The purpose of the recursion here is to collect all of the tokens until we reach the match
    // closing bracket for the opening bracket at the front of tokens.   advance() takes
    // care of collecting the tokens as we go.
    val results = new collection.mutable.ListBuffer[Token]
    def advance() {
      val token = tokens.next()
      if (token.tpe == TokenType.Eof)
        exception(MissingCloseBracket, openBracket)
      results += token
    }
    def recurse() {
      advance() // go past the open bracket
      while(tokens.head.tpe != TokenType.CloseBracket)
        if (tokens.head.tpe == TokenType.OpenBracket)
          recurse()
        else
          advance()
      advance() // go past the close bracket
    }
    recurse()
    val end = results.last.end
    results += Token.Eof
    new DelayedBlock(results.toList,
                     results.head.start, end, openBracket.filename)
  }

  /**
   * parses a block (i.e., anything in brackets). This deals with reporter blocks (a single
   * expression), command blocks (statements), and literal lists (any number of literals). The
   * initial opening bracket should still be the first token in the tokens in the DelayedBlock.
   */
  private def parseDelayedBlock(block: DelayedBlock, goalType: Int): core.Expression = {
    val tokens = block.tokens.iterator.buffered
    val openBracket = tokens.head
    if (compatible(goalType, Syntax.ReporterBlockType)) {
      tokens.next()
      val expr = resolveType(Syntax.WildcardType, parseExpression(tokens, false, goalType), null)
      val token = tokens.head
      // should be impossible for delayed block
      cAssert(token.tpe != TokenType.Eof, MissingCloseBracket, openBracket)
      cAssert(token.tpe == TokenType.CloseBracket, ExpectedCloseBracket, token)
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      new core.ReporterBlock(expr.asInstanceOf[core.ReporterApp], openBracket.start, token.end, token.filename)
    }
    else if(compatible(goalType, Syntax.CommandBlockType)) {
      tokens.next()
      var token = tokens.head
      val stmts = new core.Statements(token.filename)
      while(token.tpe != TokenType.CloseBracket) {
        // if next is an Eof, we complain and point to the open bracket. this should be impossible,
        // since it's a delayed block.
        cAssert(token.tpe != TokenType.Eof, MissingCloseBracket, openBracket)
        stmts.addStatement(parseStatement(tokens, false))
        token = tokens.head
      }
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      new core.CommandBlock(stmts, openBracket.start, token.end, token.filename)
    }
    else if (compatible(goalType, Syntax.ReporterTaskType) &&
             !block.isCommandTask &&
             !compatible(goalType, Syntax.ListType)) {
      val openBracket = tokens.next()
      val expr =
        resolveType(Syntax.WildcardType,
            parseExpression(tokens, false, Syntax.WildcardType), null)
          .asInstanceOf[core.ReporterApp]
      val closeBracket = tokens.head
      cAssert(closeBracket.tpe != TokenType.Eof, MissingCloseBracket, openBracket)
      cAssert(closeBracket.tpe == TokenType.CloseBracket, ExpectedCloseBracket, closeBracket)
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      val task = new core.prim._reportertask
      task.token = openBracket
      val app = new core.ReporterApp(task,
        openBracket.start, closeBracket.end, openBracket.filename)
      app.addArgument(expr)
      app
    }
    else if (compatible(goalType, Syntax.CommandTaskType) &&
             block.isCommandTask &&
             !compatible(goalType, Syntax.ListType)) {
      val openBracket = tokens.next()
      var token = tokens.head
      val stmts = new core.Statements(token.filename)
      while(token.tpe != TokenType.CloseBracket) {
        // if next is an Eof, we complain and point to the open bracket. this should be impossible,
        // since it's a delayed block.
        cAssert(token.tpe != TokenType.Eof, MissingCloseBracket, openBracket)
        stmts.addStatement(parseStatement(tokens, false))
        token = tokens.head
      }
      val closeBracket = token
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      val task = new core.prim._commandtask
      task.token = openBracket
      val rapp =
        new core.ReporterApp(task,
          openBracket.start, closeBracket.end, openBracket.filename)
      rapp.addArgument(
        new core.CommandBlock(stmts,
          openBracket.start, closeBracket.end, openBracket.filename))
      rapp
    }
    else if (compatible(goalType, Syntax.CodeBlockType)) {
      // Because we don't parse the inside of the code block, precisely because we don't want to define
      // legality of code in terms of the standard NetLogo requirements, we have to do a little sanity
      // checking here to make sure that at the very least, parenthesis and brackets are matched up
      // without being mixed and matched.  FD 8/19/2015
      @tailrec
      def check(remaining: Seq[Token], stack: Seq[Token] = Seq()) {
        if(remaining.isEmpty) {
          if(!stack.isEmpty) {
            if(stack.head.tpe == TokenType.OpenParen) {
              exception("Expected close paren here", block.tokens.last)
            }
          }
        } else if (remaining.head.tpe == TokenType.OpenBracket) {
          check(remaining.tail, remaining.head +: stack)
        } else if (remaining.head.tpe == TokenType.OpenParen) {
          check(remaining.tail, remaining.head +: stack)
        } else if (remaining.head.tpe == TokenType.CloseBracket) {
          if(!stack.isEmpty && stack.head.tpe == TokenType.OpenParen) {
            exception("Expected close paren before close bracket here", remaining.head)
          }
          if(stack.isEmpty || stack.head.tpe != TokenType.OpenBracket) {
            exception("Closing bracket has no matching open bracket here", remaining.head)
          }
          check(remaining.tail, stack.tail)
        } else if (remaining.head.tpe == TokenType.CloseParen) {
          if(!stack.isEmpty && stack.head.tpe == TokenType.OpenBracket) {
            exception("Expected close bracket before close paren here", remaining.head)
          }
          if(stack.isEmpty || stack.head.tpe != TokenType.OpenParen) {
            exception("Closing paren has no matching open paren here", remaining.head)
          }
          check(remaining.tail, stack.tail)
        } else {
          check(remaining.tail, stack)
        }
      }
      check(block.tokens.tail.dropRight(2)) // Drops two because of the EOF
      val tmp = new core.prim._constcodeblock(block.tokens.tail.dropRight(2))
      new core.ReporterApp(tmp,tokens.head.start,block.tokens.last.end,tokens.head.filename)
    }
    else if (compatible(goalType, Syntax.ListType)) {
      // parseLiteralList() deals with the open bracket itself, but it leaves the close bracket so
      // we can easily find out where the expression ends.  it's OK to pass a null world and
      // extensionManager here because we only ever use this code when we are parsing literal lists
      // while compiling code.  When we're reading lists from export files and such we go straight
      // to the LiteralParser through Compiler.readFromString ev 3/20/08

      val (list, closeBracket) = {
        new LiteralParser(NullImportHandler).parseLiteralList(tokens.next(), tokens)
      }
      val tmp = new core.prim._const(list)
      tmp.token = new Token("", TokenType.Literal, null)(
        openBracket.start, closeBracket.end, closeBracket.filename)
      new core.ReporterApp(tmp,
        openBracket.start, closeBracket.end, closeBracket.filename)
    }
    // we weren't actually expecting a block at all!
    else
      exception(
        "Expected " + core.TypeNames.aName(goalType) + " here, rather than a list or block.",
        block)
  }

  private class MissingPrefixException(val token: Token) extends Exception
  private class UnexpectedTokenException(val token: Token) extends Exception

  /**
   * represents a block whose contents we have not yet parsed. Since correctly parsing a block required
   * knowing its expected type, we have to do it in two passes. It will eventually be resolved into
   * an ReporterBlock, CommandBlock or a literal list. */
  private class DelayedBlock(val tokens: Seq[Token],
                             var start: Int,
                             var end: Int,
                             val file: String)
  extends core.Expression {
    def reportedType = throw new UnsupportedOperationException
    def isCommandTask =
      tokens.tail.dropWhile(_.tpe == TokenType.OpenParen)
            .headOption
            .exists(t => t.tpe == TokenType.Command || t.tpe == TokenType.CloseBracket)
  }

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
