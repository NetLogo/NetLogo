// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ core, api },
  core.{ Token, TokenType }

object Namer0 extends (Token => Token) {
  override def apply(token: Token) =
    if (token.tpe == TokenType.Bad)
      throw new api.CompilerException(token)
    else if (api.Keywords.isKeyword(token.text))
      token.copy(tpe = TokenType.Keyword)
    else Constants.get(token.text) match {
      case Some(value) =>
        token.copy(tpe = TokenType.Literal, value = value)
      case None =>
        token
    }
}
