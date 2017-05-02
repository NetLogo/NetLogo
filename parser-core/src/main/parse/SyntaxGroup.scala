// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ SourceLocation, Token }

sealed trait SyntaxGroup {
  def allTokens: Seq[Token]
  def start: SourceLocation
  def end: SourceLocation
  def location = SourceLocation(start.start, end.end, start.filename)
}

case class ParenGroup(innerGroups: Seq[SyntaxGroup], open: Token, close: Token) extends SyntaxGroup {
  val allTokens = (open +: innerGroups.flatMap(_.allTokens)) :+ close
  def start = open.sourceLocation
  def end = close.sourceLocation
}
case class BracketGroup(innerGroups: Seq[SyntaxGroup], open: Token, close: Token) extends SyntaxGroup {
  val allTokens = (open +: innerGroups.flatMap(_.allTokens)) :+ close
  def start = open.sourceLocation
  def end = close.sourceLocation
}
case class Atom(token: Token) extends SyntaxGroup {
  val allTokens = Seq(token)
  def start = token.sourceLocation
  def end = token.sourceLocation
}
