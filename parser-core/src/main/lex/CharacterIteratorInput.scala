// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.text.CharacterIterator, CharacterIterator.DONE

import org.nlogo.core.{ SourceLocation, Token }

import scala.annotation.tailrec

class CharacterIteratorInput(val iterator: CharacterIterator, var offset: Int, val filename: String) extends WrappedInput {
  def this(iterator: CharacterIterator, filename: String) = this(iterator, iterator.getIndex, filename)
  def hasNext: Boolean = iterator.current != DONE

  @tailrec
  private def longestPrefixTail(p: LexPredicate, acc: String): String = {
    val c = iterator.current
    if (c != DONE && p(c).continue) {
      iterator.next()
      longestPrefixTail(p, acc + c)
    } else
      acc
  }

  def longestPrefix(f: LexPredicate): (String, WrappedInput) =
    (longestPrefixTail(f, ""), new CharacterIteratorInput(iterator, filename))

  def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)] = {
    val offset = iterator.getIndex
    val (prefix, remainder) = longestPrefix(p)
    prefix match {
      case "" => None
      case nonEmptyString =>
        f(nonEmptyString) match {
          case None =>
            iterator.setIndex(offset)
            None
          case Some((text, tpe, tval)) =>
            Some((new Token(text, tpe, tval)(SourceLocation(offset, remainder.offset, filename)), remainder))
        }
    }
  }
}
