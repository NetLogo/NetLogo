// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ConstraintSpecification._

object BoxedValue {
  import NumericInput._
  import StringInput._
  val Defaults = Seq(
    StringInput("", StringLabel, false),
    NumericInput(0, NumberLabel),
    NumericInput(0, ColorLabel),
    StringInput("", ReporterLabel, false),
    StringInput("", CommandLabel, false))
}

object NumericInput {
  sealed trait NumericKind {
    def display: String
  }

  def label(s: String): NumericKind = {
    s match {
      case "Number" => NumberLabel
      case "Color"  => ColorLabel
    }
  }

  case object NumberLabel extends NumericKind { val display = "Number" }
  case object ColorLabel  extends NumericKind { val display = "Color" }
}

case class NumericInput(value: Double, label: NumericInput.NumericKind) extends BoxedValue {
  def name = label.display
  def constraint = NumericInputConstraintSpecification(name, value)
  def default = NumericInput(0, label)
  def multiline = false
  def asString = value.toString
  def defaultString = value.toString
}

object StringInput {
  sealed trait StringKind {
    def display: String
  }

  def label(s: String): StringKind =
    s match {
      case "String" => StringLabel
      case "String (reporter)" => ReporterLabel
      case "String (commands)" => CommandLabel
    }

  case object StringLabel   extends StringKind { val display = "String" }
  case object ReporterLabel extends StringKind { val display = "String (reporter)" }
  case object CommandLabel  extends StringKind { val display = "String (commands)" }
}

case class StringInput(value: String, label: StringInput.StringKind, multiline: Boolean) extends BoxedValue {
  def name = label.display
  def constraint = StringInputConstraintSpecification(name, value)
  def default = StringInput("", label, false)
  def asString = Dump.logoObject(value.toString)
  def defaultString = value.toString
}

object InputBox {
  val NumberLabel   = NumericInput.NumberLabel
  val ColorLabel    = NumericInput.ColorLabel
  val StringLabel   = StringInput.StringLabel
  val ReporterLabel = StringInput.ReporterLabel
  val CommandLabel  = StringInput.CommandLabel
}

sealed trait BoxedValue {
  def name: String
  def constraint: ConstraintSpecification
  def multiline: Boolean
  def default: BoxedValue
  def asString: String
  def defaultString: String
}

case class InputBox(variable: Option[String],
  left:  Int = 0, top:    Int = 0,
  right: Int = 0, bottom: Int = 0,
  boxedValue: BoxedValue = StringInput("", StringInput.StringLabel, false))
  extends Widget
  with DeclaresGlobal
  with DeclaresGlobalCommand
  with DeclaresConstraint {

  override def varName = variable.getOrElse("")

  override def default = boxedValue match {
    case NumericInput(value, _) => value
    case StringInput(value, _, _) => value
  }

  def multiline = boxedValue.multiline

  override def constraint = boxedValue.constraint
}
