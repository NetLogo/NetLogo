// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ConstraintSpecification.ChoiceConstraintSpecification

sealed trait Chooseable {
  type ChosenType <: AnyRef

  def value: ChosenType
}

object Chooseable {
  def apply(a: AnyRef): Chooseable = {
    a match {
      case s: String            => ChooseableString(s)
      case d: java.lang.Double  => ChooseableDouble(d)
      case b: java.lang.Boolean => ChooseableBoolean(b)
      case l: LogoList          => ChooseableList(l)
      case invalidElement       => throw new RuntimeException(s"Invalid chooser option $invalidElement")
    }
  }
}

case class ChooseableDouble(value: java.lang.Double) extends Chooseable {
  type ChosenType = java.lang.Double
}

case class ChooseableString(value: String) extends Chooseable {
  type ChosenType = String
}

case class ChooseableList(value: LogoList) extends Chooseable {
  type ChosenType = LogoList
}

case class ChooseableBoolean(value: java.lang.Boolean) extends Chooseable {
  type ChosenType = java.lang.Boolean
}

case class Chooser(
  variable: Option[String],
  x:  Int = 0, y:    Int = 0,
  width: Int = 0, height: Int = 0,
  display: Option[String] = None,
  choices: List[Chooseable] = Nil,
  currentChoice: Int = 0)
  extends Widget
  with DeclaresGlobal
  with DeclaresGlobalCommand
  with DeclaresConstraint {

  override def varName = variable.getOrElse("")
  override def default = choices(currentChoice).value
  override def constraint = ChoiceConstraintSpecification(choices.map(_.value), currentChoice)
}
