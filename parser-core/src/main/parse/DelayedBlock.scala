// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ prim, Expression, SourceLocation, Token, TokenType },
    prim.Lambda

object DelayedBlock {
  def apply(openBracket: Token, unterminatedTokens: Seq[Token], scope: SymbolTable): DelayedBlock = {
    ArrowLambdaScoper(openBracket +: unterminatedTokens, scope) match {
      case Some((args, body, symbols)) =>
        new ArrowLambdaBlock(
          openBracket,
          args,
          body,
          unterminatedTokens.last,
          openBracket +: unterminatedTokens :+ Token.eof(openBracket.sourceLocation.filename),
          symbols)
      case None =>
        new AmbiguousDelayedBlock(openBracket, unterminatedTokens, scope)
    }
  }
}

trait DelayedBlock extends Expression {
  def openBracket: Token
  def reportedType() = throw new UnsupportedOperationException
  def tokens: Seq[Token]
  def isCommand: Boolean
  def isArrowLambda: Boolean
  def internalScope: SymbolTable
}

class ArrowLambdaBlock(
  val openBracket:    Token,
  val arguments:      Lambda.Arguments,
  val bodyTokens:     Seq[Token],
  closingBracket:     Token,
  val allTokens:      Seq[Token],
  val internalScope:  SymbolTable,
  val sourceLocation: SourceLocation) extends DelayedBlock {

  val argNames = arguments.argumentNames

  def this(
    openBracket:    Token,
    arguments:      Lambda.Arguments,
    bodyTokens:     Seq[Token],
    closingBracket: Token,
    allTokens:      Seq[Token],
    internalScope:  SymbolTable) =
      this(openBracket, arguments, bodyTokens, closingBracket,
        allTokens, internalScope,openBracket.sourceLocation.copy(end = closingBracket.end))

  val isArrowLambda = true

  lazy val tokens = (openBracket +: bodyTokens :+ closingBracket) :+ Token.eof(openBracket.sourceLocation.filename)

  override def isCommand = bodyTokens
    .dropWhile(_.tpe == TokenType.OpenParen).headOption
    .map(_.tpe == TokenType.Command).getOrElse(true)

  override def changeLocation(newLocation: SourceLocation): ArrowLambdaBlock =
    new ArrowLambdaBlock(
      openBracket,
      arguments,
      bodyTokens,
      closingBracket,
      allTokens,
      internalScope,
      newLocation)
}

/**
 * represents a block whose contents we have not yet parsed. Since correctly parsing a block requires
 * knowing its expected type, we have to do it in two passes. It will eventually be resolved into
 * an ReporterBlock, CommandBlock or a literal list. */
class AmbiguousDelayedBlock(
  val openBracket:    Token,
  unterminatedTokens: Seq[Token],
  val internalScope:  SymbolTable,
  val sourceLocation: SourceLocation)
  extends DelayedBlock {

  def this(
    openBracket: Token,
    unterminatedTokens: Seq[Token],
    internalScope: SymbolTable) =
    this(openBracket,
      unterminatedTokens,
      internalScope,
      openBracket.sourceLocation.copy(end = unterminatedTokens.lastOption.map(_.end)
        .getOrElse(openBracket.end)))

  lazy val tokens = openBracket +: unterminatedTokens :+ Token.eof(openBracket.sourceLocation.filename)

  lazy val isCommand =
    unterminatedTokens.dropWhile(_.tpe == TokenType.OpenParen)
      .headOption
      .exists(t => t.tpe == TokenType.Command || t.tpe == TokenType.CloseBracket)

  lazy val isArrowLambda = false

  override def changeLocation(newLocation: SourceLocation): AmbiguousDelayedBlock =
    new AmbiguousDelayedBlock(openBracket, unterminatedTokens, internalScope, newLocation)
}
