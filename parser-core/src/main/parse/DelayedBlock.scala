// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Expression, SourceLocation, Token, TokenType }

object DelayedBlock {
  def apply(openBracket: Token, unterminatedTokens: Seq[Token], scope: SymbolTable): DelayedBlock = {
    ArrowLambdaScoper(openBracket +: unterminatedTokens, scope) match {
      case Some((args, body, symbols)) =>
        new ArrowLambdaBlock(openBracket, args, body, unterminatedTokens.last, symbols)
      case None =>
        new AmbiguousDelayedBlock(openBracket, unterminatedTokens, scope)
    }
  }
}

trait DelayedBlock extends Expression {
  def openBracket: Token
  def reportedType = throw new UnsupportedOperationException
  def tokens: Seq[Token]
  def isCommand: Boolean
  def isArrowLambda: Boolean
  def internalScope: SymbolTable
}

class ArrowLambdaBlock(
  val openBracket: Token,
  val argNames: Seq[String],
  val bodyTokens: Seq[Token],
  closingBracket: Token,
  val internalScope: SymbolTable,
  val sourceLocation: SourceLocation) extends DelayedBlock {

  def this(openBracket: Token, argNames: Seq[String], bodyTokens: Seq[Token], closingBracket: Token, internalScope: SymbolTable) =
      this(openBracket, argNames, bodyTokens, closingBracket,
        internalScope,openBracket.sourceLocation.copy(end = closingBracket.end))

  val isArrowLambda = true

  lazy val tokens = (openBracket +: bodyTokens :+ closingBracket) :+ Token.Eof

  override def isCommand = bodyTokens
    .dropWhile(_.tpe == TokenType.OpenParen).headOption
    .map(_.tpe == TokenType.Command).getOrElse(true)

  override def changeLocation(newLocation: SourceLocation): ArrowLambdaBlock =
    new ArrowLambdaBlock(openBracket, argNames, bodyTokens, closingBracket, internalScope, newLocation)
}

/**
 * represents a block whose contents we have not yet parsed. Since correctly parsing a block required
 * knowing its expected type, we have to do it in two passes. It will eventually be resolved into
 * an ReporterBlock, CommandBlock or a literal list. */
class AmbiguousDelayedBlock(val openBracket: Token, unterminatedTokens: Seq[Token], val internalScope: SymbolTable, val sourceLocation: SourceLocation)
  extends DelayedBlock {
    def this(openBracket: Token, unterminatedTokens: Seq[Token], internalScope: SymbolTable) =
      this(openBracket, unterminatedTokens, internalScope,
        openBracket.sourceLocation.copy(end = unterminatedTokens.lastOption.map(_.end) getOrElse openBracket.end))

  lazy val tokens = openBracket +: unterminatedTokens :+ Token.Eof

  lazy val isCommand =
    unterminatedTokens.dropWhile(_.tpe == TokenType.OpenParen)
      .headOption
      .exists(t => t.tpe == TokenType.Command || t.tpe == TokenType.CloseBracket)

  lazy val isArrowLambda = false

  override def changeLocation(newLocation: SourceLocation): AmbiguousDelayedBlock =
    new AmbiguousDelayedBlock(openBracket, unterminatedTokens, internalScope, newLocation)
}
