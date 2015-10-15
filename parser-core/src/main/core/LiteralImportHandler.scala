// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object LiteralImportHandler {
  type Parser = (Token, Iterator[Token]) => AnyRef
}

trait LiteralImportHandler {
  def parseExtensionLiteral(token: Token): AnyRef
  def parseLiteralAgentOrAgentSet(tokens: Iterator[Token], parser: LiteralImportHandler.Parser): AnyRef
}
