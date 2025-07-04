// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

import java.util.Locale

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
    def argumentSyntax: Seq[Int]
    def isVariadic:     Boolean = Syntax.isVariadic(argumentSyntax)
  }
  case class NoArguments(usesArrow: Boolean) extends Arguments {
    val argumentNames = Seq()
    def argumentTokens: Seq[Token] = Seq()
    def argumentSyntax = Seq()
  }
  case class ConciseArguments(argumentNames: Seq[String], override val argumentSyntax: Seq[Int]) extends Arguments {
    def argumentTokens: Seq[Token] = Seq()
  }
  case class UnbracketedArgument(t: Token) extends Arguments {
    def argumentNames = Seq(t.text.toUpperCase(Locale.ENGLISH))
    def argumentTokens: Seq[Token] = Seq(t)
    def argumentSyntax = Seq(Syntax.WildcardType)
  }
  case class BracketedArguments(argumentTokens: Seq[Token]) extends Arguments {
    def argumentNames  = argumentTokens.map(_.text.toUpperCase(Locale.ENGLISH))
    def argumentSyntax = argumentTokens.map(_ => Syntax.WildcardType)
  }
}
