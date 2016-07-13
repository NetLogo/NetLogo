// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ Token, TokenType }

// What's this for? See long comment in LetScoper.

object LetNamer extends TokenTransformer[Boolean] {
  def initialState = false

  override def transform(token: Token, lastTokenWasLet: Boolean): (Token, Boolean) =
    token match {
      case t @ Token(_, TokenType.Ident, _) if lastTokenWasLet =>
        (t.refine(core.prim._letname(), tpe = TokenType.Reporter), false)
      case t @ (Token(_, TokenType.Ident, "LET") |  Token(_, TokenType.Ident, "__LET")) =>
        (t, true)
      case t => (t, false)
    }
}
