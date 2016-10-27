// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{Reader => JReader, BufferedReader}

import org.nlogo.core.{ SourceLocation, Token }

import scala.annotation.tailrec

class BufferedInputWrapper(buffReader: LexReader, var offset: Int, val filename: String) extends WrappedInput {
  def this(input: BufferedReader, offset: Int, filename: String) = {
    this(new AutoGrowingBufferedReader(input), offset, filename)
  }
  def this(input: JReader, offset: Int, filename: String) = {
    this(new BufferedReader(input, 65536), offset, filename)
  }

  def nextChar: Option[Char] = {
    val readChar = buffReader.read()
    if (readChar == -1)
      None
    else
      Some(readChar.asInstanceOf[Char])
  }

  @tailrec
  private def longestPrefixTail(p: LexPredicate, acc: String): String =
    nextChar match {
      case Some(c) if p(c).continue => longestPrefixTail(p, acc + c)
      case _ => acc
    }

  override def hasNext: Boolean = {
    buffReader.mark()
    val r = buffReader.read() != -1
    buffReader.reset()
    r
  }

  override def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)] = {
    val originalOffset = offset
    val (prefix, remainder) = longestPrefix(p)
    (prefix match {
      case "" => None
      case nonEmptyString => f(nonEmptyString).map {
        case (text, tpe, tval) => (new Token(text, tpe, tval)(SourceLocation(originalOffset, remainder.offset, filename)), this)
      }
      }) orElse {
        buffReader.reset()
        offset = originalOffset
        None
      }
  }

  override def longestPrefix(f: LexPredicate): (String, WrappedInput) = {
    buffReader.mark()
    val (a, b) = (longestPrefixTail(f, ""), this)
    buffReader.reset()
    buffReader.skip(a.length) // we always go "one too far", so we have to backup
    offset += a.length
    (a, b)
  }
}
