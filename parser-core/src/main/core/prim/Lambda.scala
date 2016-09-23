// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

trait Lambda {
  def argumentNames: Seq[String]
  def synthetic: Boolean
  def closedVariables: Set[ClosedVariable]
}

object Lambda {
  def unapply(l: Lambda): Option[(Seq[String], Boolean, Set[ClosedVariable])] =
    Some((l.argumentNames, l.synthetic, l.closedVariables))
}
