// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ prim, Expression, SourceLocation, Token, TokenType },
    prim.Lambda

object DelayedBlock {
  def apply(group: BracketGroup, scope: SymbolTable): DelayedBlock = {
    ArrowLambdaScoper(group, scope) match {
      case Some((args, body, symbols)) =>
        new ArrowLambdaBlock(group, args, body, symbols, group.start.copy(end = group.end.end))
      case None =>
        new AmbiguousDelayedBlock(group, scope)
    }
  }
}

trait DelayedBlock extends Expression {
  def group: BracketGroup
  def openBracket:   Token
  def tokens:        Seq[Token]
  def isCommand:     Boolean
  def isArrowLambda: Boolean
  def internalScope: SymbolTable
  def bodyGroups:    Seq[SyntaxGroup]
  def reportedType = throw new UnsupportedOperationException
}

class ArrowLambdaBlock(
  val group:          BracketGroup,
  val arguments:      Lambda.Arguments,
  val bodyGroups:     Seq[SyntaxGroup],
  val internalScope:  SymbolTable,
  val sourceLocation: SourceLocation) extends DelayedBlock {

  def this(
    group:         BracketGroup,
    arguments:     Lambda.Arguments,
    bodyGroups:    Seq[SyntaxGroup],
    internalScope: SymbolTable) =
      this(group, arguments, bodyGroups, internalScope, group.location)

  val argNames = arguments.argumentNames

  val isArrowLambda = true

  lazy val tokens = allTokens :+ Token.Eof

  def openBracket = group.open

  def allTokens = group.allTokens

  // TODO: Needs group-aware rewrite
  override def isCommand = bodyGroups.flatMap(_.allTokens)
    .dropWhile(_.tpe == TokenType.OpenParen).headOption
    .map(_.tpe == TokenType.Command).getOrElse(true)

  override def changeLocation(newLocation: SourceLocation): ArrowLambdaBlock =
    new ArrowLambdaBlock(
      group,
      arguments,
      bodyGroups,
      internalScope,
      newLocation)
}

/**
 * represents a block whose contents we have not yet parsed. Since correctly parsing a block requires
 * knowing its expected type, we have to do it in two passes. It will eventually be resolved into
 * an ReporterBlock, CommandBlock or a literal list. */
class AmbiguousDelayedBlock(
  val group:          BracketGroup,
  val internalScope:  SymbolTable,
  val sourceLocation: SourceLocation)
  extends DelayedBlock {

  def this(group: BracketGroup, internalScope: SymbolTable) = this(group, internalScope, group.location)

  def bodyGroups = group.innerGroups

  def openBracket = group.open

  lazy val tokens = group.allTokens :+ Token.Eof

  lazy val isCommand =
    tokens.drop(1).dropWhile(_.tpe == TokenType.OpenParen)
      .headOption
      .exists(t => t.tpe == TokenType.Command || t.tpe == TokenType.CloseBracket)

  lazy val isArrowLambda = false

  override def changeLocation(newLocation: SourceLocation): AmbiguousDelayedBlock =
    new AmbiguousDelayedBlock(group, internalScope, newLocation)
}
