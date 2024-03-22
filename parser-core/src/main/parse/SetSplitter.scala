// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Command, Token, TokenType }
import org.nlogo.core.Fail._
import org.nlogo.core.prim.{ _multiset, _set }

object SplitSet {
  def apply(s: _set, tokens: BufferedIterator[Token]): Command = {
    tokens.head match {
      case _ @ Token(_, TokenType.OpenBracket, _) =>
        var sets: Seq[Token] = Seq()
        tokens.next()
        while (tokens.hasNext && tokens.head.tpe != TokenType.CloseBracket) {
          val token = tokens.head
          if (token.text != "set") {
            sets = sets :+ token
          }
          tokens.next()
        }
        // pop the close bracket...
        if (tokens.hasNext) {
          tokens.next()
        }

        if (sets.length == 0) {
          exception("The list of variables names given to SET must contain at least one item.", s.token)
        }

        val multi = _multiset(sets)
        s.token.refine(multi, text = "_multiset")
        multi

      case _ =>
        s

    }
  }
}
