// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api, api.{ Token, TokenType }

object Namer0 extends (Token => Token) {
  override def apply(token: Token) =
    if (api.Keywords.isKeyword(token.text))
      token.copy(tpe = TokenType.Keyword)
    else Constants.get(token.text) match {
      case Some(value) =>
        token.copy(tpe = TokenType.Literal, value = value)
      case None =>
        token
    }
}
