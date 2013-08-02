// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, parse0, prim },
  api.{ Token, TokenType },
  parse0.CountedIterator

object LetStuffer {

  def stuffLet(token: Token, lets: Vector[api.Let], it: CountedIterator[Token]): Token = {
    (token.tpe, token.value) match {
      case (TokenType.Command, let: prim._let) =>
        // LetScoper constructed Let objects, but it didn't stash them
        // in the prim._let objects. we do that here, so that LetScoper
        // doesn't depend on prim._let - ST 5/2/13
        let.let = lets.find(let => let.start == it.count + 1).get
      case _ =>
    }
    token
  }

}
