// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

// annoying for this to exist, but I can't see a better way to get at the
// Scala inner objects from Java - ST 7/7/11

import org.nlogo.api.TokenType

object TokenTypeJ {
  final val OpenParen    = TokenType.OpenParen
  final val CloseParen   = TokenType.CloseParen
  final val OpenBracket  = TokenType.OpenBracket
  final val CloseBracket = TokenType.CloseBracket
  final val OpenBrace    = TokenType.OpenBrace
  final val CloseBrace   = TokenType.CloseBrace
  final val Literal      = TokenType.Literal
  final val Ident        = TokenType.Ident
  final val Comma        = TokenType.Comma
  final val Comment      = TokenType.Comment
  final val Bad          = TokenType.Bad
}
