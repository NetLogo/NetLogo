// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ I18N, Token, TokenType }
import org.nlogo.core.Fail._
import org.nlogo.core.prim.{ _abstractset, _multiset, _set }

object SplitSet {
  def apply(s: _set, tokens: BufferedIterator[Token]): _abstractset = {
    tokens.head match {
      case _ @ Token(_, TokenType.OpenBracket, _) =>
        val set = recurseScopes(tokens)

        // pop the final close bracket
        if (tokens.hasNext)
          tokens.next()

        s.token.refine(set, text = "_multiset")

        set

      case _ =>
        s

    }
  }

  def recurseScopes(tokens: BufferedIterator[Token]): _abstractset = {
    val token = tokens.head
    var sets = Seq[_abstractset]()

    tokens.next()

    while (tokens.hasNext && tokens.head.tpe != TokenType.CloseBracket) {
      val token = tokens.head

      if (token.tpe == TokenType.OpenBracket) {
        sets = sets :+ recurseScopes(tokens)
      } else if (token.text != "set") {
        val splitSet = new _set()

        splitSet.token = token

        sets = sets :+ splitSet
      }

      tokens.next()
    }

    if (sets.length == 0)
      exception(I18N.errors.getN("compiler.MultiAssign.emptyVarList", "SET"), token)

    val multi = _multiset(sets)

    multi.token = token

    multi
  }
}
