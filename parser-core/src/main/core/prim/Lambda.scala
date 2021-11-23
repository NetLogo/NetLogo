// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

trait Lambda {
  def arguments: Lambda.Arguments
  def closedVariables: Set[ClosedVariable]
  def argumentNames: Seq[String] = arguments.argumentNames
  def minArgCount: Int = argumentNames.length
  def synthetic: Boolean = arguments match {
    case Lambda.ConciseArguments(_, _) => true
    case _ => false
  }
}

object Lambda {
  def unapply(l: Lambda): Option[(Seq[String], Boolean, Set[ClosedVariable])] =
    Some((l.argumentNames, l.synthetic, l.closedVariables))

  sealed trait Arguments {
    def argumentNames:  Seq[String]
    def argumentTokens: Seq[Token]
    def isVariadic:     Boolean = false
  }
  case class NoArguments(usesArrow: Boolean) extends Arguments {
    val argumentNames = Seq()
    def argumentTokens: Seq[Token] = Seq()
  }
  case class ConciseArguments(argumentNames: Seq[String], override val isVariadic: Boolean = false) extends Arguments {
    def argumentTokens: Seq[Token] = Seq()
  }
  case class UnbracketedArgument(t: Token) extends Arguments {
    def argumentNames = Seq(t.text.toUpperCase)
    def argumentTokens: Seq[Token] = Seq(t)
  }
  case class BracketedArguments(argumentTokens: Seq[Token]) extends Arguments {
    def argumentNames = argumentTokens.map(_.text.toUpperCase)
  }
}
