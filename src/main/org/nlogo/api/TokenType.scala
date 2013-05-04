// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract sealed trait TokenType

object TokenType {
  case object EOF extends TokenType
  case object OpenParen extends TokenType
  case object CloseParen extends TokenType
  case object OpenBracket extends TokenType
  case object CloseBracket extends TokenType
  case object OpenBrace extends TokenType
  case object CloseBrace extends TokenType
  case object Literal extends TokenType
  case object Ident extends TokenType
  case object Command extends TokenType
  case object Reporter extends TokenType
  case object Keyword extends TokenType
  case object Comma extends TokenType
  case object Comment extends TokenType
  case object Bad extends TokenType        // characters the tokenizer couldn't digest
  case object Extension extends TokenType  // extension literals (for export-world of extension types)
}
