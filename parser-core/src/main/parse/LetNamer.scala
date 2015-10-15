// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ Token, TokenType }

// What's this for? See long comment in LetScoper.

object LetNamer {
  def apply(xs: Iterator[Token]): Iterator[Token] = {
    var lastTokenWasLet = false
    xs.map{
      case t @ Token(_, TokenType.Ident, _) if lastTokenWasLet =>
        lastTokenWasLet = false
        t.refine(core.prim._letname(), tpe = TokenType.Reporter)
      case t @ Token(_, TokenType.Ident, "LET") =>
        lastTokenWasLet = true
        t
      case t @ Token(_, TokenType.Ident, "__LET") =>
        lastTokenWasLet = true
        t
      case t =>
        lastTokenWasLet = false
        t
    }
  }
}
