// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

sealed trait LexStates {
  def continue: Boolean
  def or(d: LexStates): LexStates
}

case object Accept extends LexStates {
  val continue = true
  override def or(d: LexStates): LexStates = Accept
}

case object Finished extends LexStates {
  val continue = false
  override def or(d: LexStates): LexStates = {
    d match {
      case Accept => Accept
      case _ => Finished
    }
  }
}

case object Error extends LexStates {
  val continue = false
  override def or(d: LexStates): LexStates = d
}
