// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import TokenType._

object TokenDSL {
  def `{`: Token             = Token("{", OpenBrace, null)(0, 0, "test")
  def `}`: Token             = Token("}", CloseBrace, null)(0, 0, "test")
  def `[`: Token             = Token("[", OpenBracket, null)(0, 0, "test")
  def `]`: Token             = Token("]", CloseBracket, null)(0, 0, "test")
  def id(str: String): Token = Token(str, Ident, str.toUpperCase)(0, 0, "test")
  def lit(v: Int): Token     = Token(v.toString, Literal, Double.box(v.toDouble))(0, 0, "test")
  def ex(str: String): Token = Token(str, Extension, str)(0, 0, "test")
  def eof: Token             = Token("eof", Eof, null)(0, 0, "test")

  def tokenIterator(ts: Token*): Iterator[Token] = ts.iterator ++ Iterator(eof)
}
