// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import Fail.{ cAssert, exception }
import org.nlogo.api.{ LogoList, Nobody, Syntax, Token, TokenType, TypeNames }
import Syntax.compatible
import org.nlogo.nvm.{ Command, Instruction, Procedure, Referenceable, Reporter}
import org.nlogo.prim._
import org.nlogo.parse0.LiteralParser

/**
 * The actual NetLogo parser.
 * The jargon here is a bit different from the usual NetLogo terminology:
 *  - "command" is an actual command token itself, e.g., show, run.
 *  - "reporter" is an actual reporter itself, e.g., +, round, with.
 *  - "statement" is a syntactic form with no value and a command as head (e.g., show 5)
 *  - "expression" is a syntactic form which can occur as an argument to a command or to a
 *    reporter. expressions denote values. there are two basic kinds of expression:
 *     - reporter applications (infix or prefix). Note that this is reporter in the internal sense,
 *       which includes variables and literals. So these include, e.g., turtles with [ true ], 5 +
 *       10, 5, [1 2 3].
 *     - blocks. command and reporter blocks are expressions of this type.  a command block contains
 *       zero or more statements, while a reporter block contains exactly one expression.
 */

class ExpressionParser(
  procedure: Procedure,
  taskNumbers: Iterator[Int] = Iterator.from(1)) {

  /**
   * one less than the lowest valid operator precedence. See
   * Syntax.
   */
  private val MinPrecedence = -1

  private var result = List[ProcedureDefinition]()

  /**
   * parses a procedure. Procedures are a bunch of statements (not a block of statements, that's
   * something else), and so are parsed as such. */
  def parse(tokens: Iterator[Token]): Seq[ProcedureDefinition] = {
    result = Nil
    val buffered = tokens.buffered
    val stmts = new Statements(buffered.head.filename)
    while(buffered.head.tpe != TokenType.Eof)
      stmts.addStatement(parseStatement(buffered, false))
    result ::= new ProcedureDefinition(procedure, stmts)
    result
  }

  /**
   * parses a statement.
   */
  private def parseStatement(tokens: BufferedIterator[Token], variadic: Boolean): Statement = {
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
        val stmt = new Statement(token.value.asInstanceOf[Command], token.start, token.end, token.filename)
        if(variadic && isVariadic(stmt.instruction)) parseVarArgs(stmt, tokens, MinPrecedence)
        else parseArguments(stmt, tokens, MinPrecedence)
        stmt
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
  private def parseArguments(app: Application, tokens: BufferedIterator[Token], precedence: Int) {
    val right = app.instruction.syntax.right
    val optional = app.instruction.syntax.takesOptionalCommandBlock
    for(i <- 0 until app.instruction.syntax.rightDefault) {
      val arg = parseArgExpression(tokens, precedence, app, right(i min (right.size - 1)))
      app.addArgument(arg)
      app.end = arg.end
    }
    if(optional)
      if(tokens.head.tpe == TokenType.OpenBracket) {
        val arg = parseArgExpression(tokens, precedence, app, right.last)
        app.addArgument(arg)
        app.end = arg.end
      }
      else {
        // synthesize an empty block so that later phases of compilation will be dealing with a
        // consistent number of arguments - ST 3/4/08
        val file = tokens.head.filename
        app.addArgument(new CommandBlock(new Statements(file), app.end, app.end, file))
      }
    // check all types
    resolveTypes(app)
  }

  /**
   * parses arguments for commands and reporters. Arguments consist of some number of expressions
   * (possibly 0). We'll continue parsing expressions until we encounter a closing parenthesis, an
   * error, or a lower precedence binary reporter. In the last case, it turns out that this app
   * can't have a non-default number of args after all, so we assert that it doesn't. Type
   * resolution is then performed.
   */
  private def parseVarArgs(app: Application, tokens: BufferedIterator[Token], precedence: Int) {
    var done = false
    var token = tokens.head
    var argNumber = 0
    val right = app.instruction.syntax.right
    def goalType = right(argNumber min (right.size - 1))
    while(!done) {
      if(token.tpe == TokenType.CloseParen)
        done = true
      else if(token.tpe == TokenType.Reporter &&
              goalType != Syntax.ReporterTaskType &&
              token.value.asInstanceOf[Reporter].syntax.isInfix) {
        // we can be confident that any infix op still in tokens
        // at this point is lower precedence, or we would already
        // have consumed it. so if we have a non-default number of
        // args, this is definitely illegal.
        cAssert(app.size == app.instruction.syntax.totalDefault, InvalidVariadicContext, app)
        done = true
      }
      // note: if it's a reporter, it must be the beginning
      // of the next arg.
      else {
        val arg = parseArgExpression(tokens, precedence, app, goalType)
        app.addArgument(arg)
        app.end = arg.end
        token = tokens.head
      }
      argNumber += 1
    }
    // check all types
    resolveTypes(app)
  }

  /**
   * determines whether an instruction allows a variable number of args. This should maybe be moved
   * into Syntax, where it could be made more efficient.
   */
  private def isVariadic(ins: Instruction): Boolean =
    ins.syntax.right.exists(compatible(_, Syntax.RepeatableType))

  /**
   * this is used for generating an error message when some arguments are found to be missing
   */
  private def missingInput(app: Application, right: Boolean): String = {
    val syntax = app.instruction.syntax
    val rightArgs = syntax.right.map(TypeNames.aName(_).replaceFirst("anything", "any input"))
    val left = syntax.left
    val result =
      if(right && isVariadic(app.instruction) && syntax.minimum == 0)
        app.instruction.displayName + " expected " + syntax.rightDefault + " input" + (if(syntax.rightDefault > 1) "s" else "") +
        " on the right or any number of inputs when surrounded by parentheses"
      else
        app.instruction.displayName + " expected " + (if(isVariadic(app.instruction)) "at least " else "") +
        (if(right) syntax.rightDefault + " input" + (if(syntax.rightDefault > 1) "s" else "") +
                    (if(syntax.isInfix) " on the right" else "")
         else TypeNames.aName(left) + " on the left.")
    if(!right)
      result
    else if(rightArgs.forall(_ == "any input"))
      result + "."
    else if(rightArgs.size == 1)
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
  private def resolveTypes(app: Application) {
    val syntax = app.instruction.syntax
    var actual1 = 0
    // first look at left arg, if any
    if(syntax.isInfix) {
      val tpe = syntax.left
      // this shouldn't really be possible here...
      cAssert(app.size >= 1, missingInput(app, false), app)
      app.replaceArg(0, resolveType(tpe, app(0), app.instruction.displayName))
      // the first right arg is the second arg.
      actual1 = 1
    }
    // look at right args from left-to-right...
    var formal1 = 0
    val types = syntax.right
    while(formal1 < types.length && !compatible(Syntax.RepeatableType, types(formal1))) {
      if(formal1 == types.length - 1 && app.size == types.length - 1 &&
         compatible(Syntax.OptionalType, types(formal1)))
        return
      cAssert(app.size > actual1, missingInput(app, true), app)
      app.replaceArg(actual1, resolveType(types(formal1), app(actual1), app.instruction.displayName))
      formal1 += 1
      actual1 += 1
    }
    if(formal1 < types.length) {
      // then we encountered a repeatable arg, so we look at right args from right-to-left...
      var actual2 = app.size - 1
      var formal2 = types.length - 1
      while(formal2 >= 0 && !compatible(Syntax.RepeatableType, types(formal2))) {
        cAssert(app.size > actual2 && actual2 > -1, missingInput(app, true), app)
        app.replaceArg(actual2, resolveType(types(formal2), app(actual2), app.instruction.displayName))
        formal2 -= 1
        actual2 -= 1
      }
      // now we check any repeatable args...
      while(actual1 <= actual2) {
        app.replaceArg(actual1, resolveType(types(formal1), app(actual1), app.instruction.displayName))
        actual1 += 1
      }
    }
  }

  /**
   * resolves the type of an expression. We call this "resolution" instead of "checking" because
   * sometimes the expression needs further parsing or processing depending on its context and
   * expected type. For example, delayed blocks need to be parsed here based on what they're
   * expected to be, and reference types need some processing as well. The caller should replace the
   * expr it passed in with the one returned, as it may be different.
   */
  private def resolveType(goalType: Int, originalArg: Expression, instruction: String): Expression = {
    // now that we know the type, finish parsing any blocks
    val arg = originalArg match {
      case block: DelayedBlock => parseDelayedBlock(block, goalType)
      case _ => originalArg
    }
    cAssert(compatible(goalType, arg.reportedType),
            instruction + " expected this input to be " + TypeNames.aName(goalType) + ", but got " +
            TypeNames.aName(arg.reportedType) + " instead", arg)
    if(goalType == Syntax.ReferenceType) {
      // we can be sure this cast will work, because otherwise the assert above would've failed (no
      // Expression other than a ReporterApp can have type ReferenceType, which it must or we
      // wouldn't be here). there has to be a better way to do this, though...
      val rApp = arg.asInstanceOf[ReporterApp]
      cAssert(rApp.reporter.isInstanceOf[Referenceable], ExpectedReferencable, arg)
      rApp.reporter = new _reference(rApp.reporter.asInstanceOf[Referenceable].makeReference)
    }
    arg
  }

  /**
   * a wrapper around parseExpressionInternal for parsing expressions in non-argument positions
   * (basically only inside parens or reporter blocks). These expressions always have
   * MinPrecedence, so we don't need that arg.
   *
   * Package protected for unit testing.
   *
   * @param tokens   the input token stream
   * @param variadic whether to treat this expression as possibly variadic
   */
  def parseExpression(tokens: BufferedIterator[Token], variadic: Boolean, goalType: Int): Expression = {
    try { parseExpressionInternal(tokens, variadic, MinPrecedence, goalType) }
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
  private def parseArgExpression(tokens: BufferedIterator[Token], precedence: Int, app: Application, goalType: Int): Expression = {
    try { parseExpressionInternal(tokens, false, precedence, goalType) }
    catch {
      case e: MissingPrefixException => exception(missingInput(app, true), app)
      case e: UnexpectedTokenException => exception(missingInput(app, true), app)
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
  private def parseExpressionInternal(tokens: BufferedIterator[Token], variadic: Boolean, precedence: Int, goalType: Int): Expression = {
    var token = tokens.head
    val wantAnyTask = goalType == (Syntax.ReporterTaskType | Syntax.CommandTaskType)
    val wantReporterTask = wantAnyTask || goalType == Syntax.ReporterTaskType
    val wantCommandTask = wantAnyTask || goalType == Syntax.CommandTaskType
    val expr: Expression =
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
        case TokenType.Reporter | TokenType.Literal =>
          tokens.next()
          val (reporter, rApp) = token.tpe match {
            case TokenType.Literal =>
              val r = ExpressionParser.makeLiteralReporter(token.value)
              r.token(token)
              (r, new ReporterApp(r, token.start, token.end, token.filename))
            case TokenType.Reporter =>
              val r = token.value.asInstanceOf[Reporter]
              // the "|| wantReporterTask" is needed or the concise syntax wouldn't work for infix
              // reporters, e.g. "map + ..."
              if(!r.syntax.isInfix || wantReporterTask)
                (r, new ReporterApp(r, token.start, token.end, token.filename))
              else {
                // this is a bit of a hack, but it's not terrible.  _minus is allowed to be unary
                // (negation) but only if it's missing a left argument and is in a possibly variadic
                // context (the first thing in a set of parens, basically).
                if(!r.isInstanceOf[_minus] || !variadic)
                  throw new MissingPrefixException(token)
                val r2 = new _unaryminus
                r2.token(token)
                (r2, new ReporterApp(r2, token.start, token.end, token.filename))
              }
            case _ =>
              sys.error("unexpected token type: " + token.tpe)
          }
          // handle the case of the concise task syntax, where I can write e.g. "map + ..." instead
          // of "map [?1 + ?2] ...".  for the task primitive itself we allow this even for literals
          // and nullary reporters, for the other primitives like map we require the reporter to
          // take at least one input (since otherwise a simple "map f xs" wouldn't evaluate f).  the
          // !variadic check is to prevent "map (f a) ..." from being misparsed.
          if(wantReporterTask && !variadic && (wantAnyTask || reporter.syntax.totalDefault > 0)) {
            val task = new _reportertask
            task.token(reporter.token)
            val taskApp = new ReporterApp(task, reporter.token.start, reporter.token.end, reporter.token.filename)
            taskApp.addArgument(rApp)
            for(argNumber <- 1 to reporter.syntax.totalDefault) {
              val lv = new _taskvariable(argNumber)
              lv.token(reporter.token)
              rApp.addArgument(new ReporterApp(lv, reporter.token.start, reporter.token.end, reporter.token.filename))
            }
            taskApp
          }
          // the normal case
          else {
            if(variadic && isVariadic(rApp.instruction))
              parseVarArgs(rApp, tokens, reporter.syntax.precedence)
            else
              parseArguments(rApp, tokens, reporter.syntax.precedence)
            rApp
          }
        // handle the case of the concise task syntax, where I can write e.g. "foreach xs print"
        // instead of "foreach xs [ print ? ]"
        case TokenType.Command if wantCommandTask =>
          tokens.next()
          val stmt = new Statement(token.value.asInstanceOf[Command], token.start, token.end, token.filename)
          val stmts = new Statements(token.filename)
          stmts.addStatement(stmt)
          val taskProcedure = new Procedure(
            false, "__task-" + taskNumbers.next(), token, Seq(), parent = procedure)
          procedure.children += taskProcedure
          taskProcedure.pos = token.start
          taskProcedure.end = token.end
          result ::= new ProcedureDefinition(taskProcedure, stmts)
          val task = new _commandtask(taskProcedure)
          task.token(token)
          for(argNumber <- 1 to stmt.command.syntax.totalDefault) {
            val lv = new _taskvariable(argNumber)
            lv.token(token)
            stmt.addArgument(new ReporterApp(lv, token.start, token.end, token.filename))
          }
          if(stmt.command.syntax.takesOptionalCommandBlock)
            // synthesize an empty block so that later phases of compilation will be dealing with a
            // consistent number of arguments - ST 3/4/08
            stmt.addArgument(
              new CommandBlock(
                new Statements(token.filename), token.start, token.end, token.filename))
          new ReporterApp(task, token.start, token.end, token.filename)
        case _ =>
          // here we throw a temporary exception, since we don't know yet what this error means... It
          // generally either means MissingInputOnRight or ExpectedReporter.
          throw new UnexpectedTokenException(token)
      }
    parseMore(expr, tokens, precedence)
  }

  /**
   * possibly parses the rest of the current expression. Expressions are parsed in a slightly
   * strange way. First we parse an expr, then we look ahead to see if the next token is an infix
   * operator. If so, we use what we've seen so far as its first arg, and then parse its other
   * arguments. Then we repeat. The result of all of this is the actual expr.
   */
  def parseMore(originalExpr: Expression, tokens: BufferedIterator[Token], precedence: Int): Expression = {
    var expr = originalExpr
    var done = false
    while(!done) {
      val token = tokens.head
      if(token.tpe == TokenType.Reporter) {
        val reporter = token.value.asInstanceOf[Reporter]
        val syntax = reporter.syntax
        if(syntax.isInfix && (syntax.precedence > precedence ||
                              (syntax.isRightAssociative && syntax.precedence == precedence))) {
          tokens.next()
          // note: this actually shouldn't be possible here, because this should never be called
          // with null expr, but better safe than sorry...
          cAssert(expr != null, MissingInputOnLeft, token)
          val tmp = new ReporterApp(reporter, expr.start, token.end, token.filename)
          tmp.addArgument(expr)
          parseArguments(tmp, tokens, syntax.precedence)
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
      if(token.tpe == TokenType.Eof)
        exception(MissingCloseBracket, openBracket)
      results += token
    }
    def recurse() {
      advance() // go past the open bracket
      while(tokens.head.tpe != TokenType.CloseBracket)
        if(tokens.head.tpe == TokenType.OpenBracket) recurse()
        else advance()
      advance() // go past the close bracket
    }
    recurse()
    val end = results.last.end
    results += Token.Eof
    new DelayedBlock(results.readOnly,
                     results.head.start, end, openBracket.filename)
  }

  /**
   * parses a block (i.e., anything in brackets). This deals with reporter blocks (a single
   * expression), command blocks (statements), and literal lists (any number of literals). The
   * initial opening bracket should still be the first token in the tokens in the DelayedBlock.
   */
  private def parseDelayedBlock(block: DelayedBlock, goalType: Int): Expression = {
    val tokens = block.tokens.iterator.buffered
    val openBracket = tokens.head
    if(compatible(goalType, Syntax.ReporterBlockType)) {
      tokens.next()
      val expr = resolveType(Syntax.WildcardType, parseExpression(tokens, false, goalType), null)
      val token = tokens.head
      cAssert(token.tpe != TokenType.Eof, MissingCloseBracket, openBracket) // should be impossible for delayed block
      cAssert(token.tpe == TokenType.CloseBracket, ExpectedCloseBracket, token)
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      new ReporterBlock(expr.asInstanceOf[ReporterApp], openBracket.start, token.end, token.filename)
    }
    else if(compatible(goalType, Syntax.CommandBlockType)) {
      tokens.next()
      var token = tokens.head
      val stmts = new Statements(token.filename)
      while(token.tpe != TokenType.CloseBracket) {
        // if next is an Eof, we complain and point to the open bracket. this should be impossible,
        // since it's a delayed block.
        cAssert(token.tpe != TokenType.Eof, MissingCloseBracket, openBracket)
        stmts.addStatement(parseStatement(tokens, false))
        token = tokens.head
      }
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      new CommandBlock(stmts, openBracket.start, token.end, token.filename)
    }
    else if(compatible(goalType, Syntax.ReporterTaskType) &&
            !block.isCommandTask &&
            !compatible(goalType, Syntax.ListType)) {
      val openBracket = tokens.next()
      val expr = resolveType(Syntax.WildcardType, parseExpression(tokens, false, Syntax.WildcardType), null).asInstanceOf[ReporterApp]
      val closeBracket = tokens.head
      cAssert(closeBracket.tpe != TokenType.Eof, MissingCloseBracket, openBracket) // should be impossible for delayed block
      cAssert(closeBracket.tpe == TokenType.CloseBracket, ExpectedCloseBracket, closeBracket)
      // the origin of the block are based on the positions of the brackets.
      tokens.next()
      val task = new _reportertask
      task.token(openBracket)
      val app = new ReporterApp(task, openBracket.start, closeBracket.end, openBracket.filename)
      app.addArgument(expr)
      app
    }
    else if(compatible(goalType, Syntax.CommandTaskType) &&
            block.isCommandTask &&
            !compatible(goalType, Syntax.ListType)) {
      val openBracket = tokens.next()
      var token = tokens.head
      val stmts = new Statements(token.filename)
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
      val taskProcedure = new Procedure(
        false, "__task-" + taskNumbers.next(), openBracket, Seq(), parent = procedure)
      procedure.children += taskProcedure
      taskProcedure.pos = openBracket.start
      taskProcedure.end = closeBracket.end
      result ::= new ProcedureDefinition(taskProcedure, stmts)
      val task = new _commandtask(taskProcedure)
      task.token(openBracket)
      new ReporterApp(task, openBracket.start, closeBracket.end, openBracket.filename)
    }
    else if(compatible(goalType, Syntax.ListType)) {
      // parseLiteralList() deals with the open bracket itself, but it leaves the close bracket so
      // we can easily find out where the expression ends.  it's OK to pass a null world and
      // extensionManager here because we only ever use this code when we are parsing literal lists
      // while compiling code.  When we're reading lists from export files and such we go straight
      // to the LiteralParser through Compiler.readFromString ev 3/20/08
      val tmp = ExpressionParser.makeLiteralReporter(
        new LiteralParser(null, null, null).parseLiteralList(tokens.next(), tokens))
      val token = tokens.next()
      tmp.token(new Token("", TokenType.Literal, null)(openBracket.start, token.end, token.filename))
      new ReporterApp(tmp, openBracket.start, token.end, token.filename)
    }
    // we weren't actually expecting a block at all!
    else exception("Expected " + TypeNames.aName(goalType) + " here, rather than a list or block.", block)
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
  extends Expression {
    def reportedType = throw new UnsupportedOperationException
    def accept(v: AstVisitor) = throw new UnsupportedOperationException
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
  private val ExpectedReferencable = "Expected a patch variable here."
  private val ExpectedReporter = "Expected reporter."
  private val InvalidVariadicContext = "To use a non-default number of inputs, you need to put parentheses around this."
  private val MissingCloseBracket = "No closing bracket for this open bracket."
  private val MissingCloseParen = "No closing parenthesis for this open parenthesis."
  private val MissingInputOnLeft = "Missing input on the left."

}

object ExpressionParser {
  def makeLiteralReporter(value: AnyRef): Reporter =
    value match {
      case b: java.lang.Boolean => new _constboolean(b)
      case d: java.lang.Double => new _constdouble(d)
      case l: LogoList => new _constlist(l)
      case s: String => new _conststring(s)
      case Nobody => new _nobody
      case _ => throw new IllegalArgumentException(value.getClass.getName)
    }
}
