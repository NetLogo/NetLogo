// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// represents an closed-over variable within a lambda. See _commandlambda and _reporterlambda for more information
sealed trait ClosedVariable {
  def name: String
}

case class ClosedLet(let: Let) extends ClosedVariable {
  override def name = let.name
}

case class ClosedLambdaVariable(name: String) extends ClosedVariable
