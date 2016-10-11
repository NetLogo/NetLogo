// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model

import scala.util.{ Failure, Success, Try }

sealed trait ConversionResult {
  def model: Model
  def hasErrors: Boolean
  def mergeResult(res: => ConversionResult): ConversionResult
  def addError(es: => ConversionError): ConversionResult
  def updateModel(m: => Model): ConversionResult
}

object ConversionError {
  def apply(e: Exception, componentDescription: String, conversionDescription: String): ConversionError =
    ConversionError(Seq(e), componentDescription, conversionDescription)
}

case class ConversionError(errors: Seq[Exception], componentDescription: String, conversionDescription: String)

sealed trait FailedConversionResult extends ConversionResult {
  def errors: Seq[ConversionError]
  def hasErrors = true
}

/* This class reflects an entirely successful conversion */
case class SuccessfulConversion(originalModel: Model, model: Model) extends ConversionResult {
  def hasErrors = false
  def mergeResult(res: => ConversionResult): ConversionResult =
    res match {
      case SuccessfulConversion(_, m) => SuccessfulConversion(originalModel, m)
      case ConversionWithErrors(_, m, error) => ConversionWithErrors(originalModel, m, error)
      case ec: ErroredConversion => ec
    }
  def addError(es: => ConversionError): ConversionResult =
    ConversionWithErrors(originalModel, model, es)
  def updateModel(m: => Model): ConversionResult =
    SuccessfulConversion(originalModel, m)
}

object ConversionWithErrors {
  def apply(originalModel: Model, model: Model, error: ConversionError): ConversionWithErrors =
    ConversionWithErrors(originalModel, model, Seq(error))
}

/* This class reflects a peripheral error in conversion, perhaps a widget or behaviorspace
 * section wouldn't convert */
case class ConversionWithErrors(originalModel: Model, model: Model, errors: Seq[ConversionError])
  extends FailedConversionResult {
  override def hasErrors = true

  def mergeResult(res: => ConversionResult): ConversionResult =
    res match {
      case SuccessfulConversion(_, m)     => ConversionWithErrors(originalModel, m, errors)
      case ConversionWithErrors(_, m, es) => ConversionWithErrors(originalModel, m, errors ++ es)
      case ec: ErroredConversion => ec
    }

  def addError(es: => ConversionError): ConversionResult =
    if (es.errors.isEmpty) this
    else ConversionWithErrors(originalModel, model, errors :+ es)
  def updateModel(m: => Model): ConversionResult =
    ConversionWithErrors(originalModel, m, errors)
}

/* This reflect a hard, non-recoverable error in conversion, typically a code tab that will
 * not compile */
case class ErroredConversion(model: Model, error: ConversionError) extends FailedConversionResult {
  def errors = Seq(error)
  def updateModel(m: => Model): ConversionResult = this
  def addError(es: => ConversionError): ConversionResult = this
  def mergeResult(res: => ConversionResult): ConversionResult = this
}
