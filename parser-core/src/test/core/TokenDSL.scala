// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import TokenType._

object TokenDSL {
  private val testLocation = SourceLocation(0, 0, "test")
  def `{`: Token               = Token("{", OpenBrace, null)(testLocation)
  def `}`: Token               = Token("}", CloseBrace, null)(testLocation)
  def `[`: Token               = Token("[", OpenBracket, null)(testLocation)
  def `]`: Token               = Token("]", CloseBracket, null)(testLocation)
  def `(`: Token               = Token("(", OpenParen, null)(testLocation)
  def `)`: Token               = Token(")", CloseParen, null)(testLocation)
  def id(str: String): Token   = Token(str, Ident, str.toUpperCase)(testLocation)
  def lit(v: Int): Token       = Token(v.toString, Literal, Double.box(v.toDouble))(testLocation)
  def ex(str: String): Token   = Token(str, Extension, str)(testLocation)
  def `;`(comment: String): Token = Token(";" + comment, Comment, comment)(testLocation)
  def eof: Token               = Token("eof", Eof, null)(testLocation)

  def tokenIterator(ts: Token*): Iterator[Token] = ts.iterator ++ Iterator(eof)
}
