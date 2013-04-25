// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract sealed trait TokenType
object TokenType {
  case object EOF extends TokenType
  case object OPEN_PAREN extends TokenType
  case object CLOSE_PAREN extends TokenType
  case object OPEN_BRACKET extends TokenType
  case object CLOSE_BRACKET extends TokenType
  case object OPEN_BRACE extends TokenType
  case object CLOSE_BRACE extends TokenType
  case object CONSTANT extends TokenType
  case object IDENT extends TokenType
  case object COMMAND extends TokenType
  case object REPORTER extends TokenType
  case object KEYWORD extends TokenType
  case object COMMA extends TokenType
  case object COMMENT extends TokenType
  case object VARIABLE extends TokenType   // built in variables only
  case object BAD extends TokenType        // characters the tokenizer couldn't digest
  case object LITERAL extends TokenType    // a literal, untokened string (for external type dumps)
}
