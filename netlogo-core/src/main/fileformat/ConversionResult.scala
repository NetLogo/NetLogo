// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model

sealed trait ConversionResult {
  def model: Model
  def isFailure: Boolean
}

sealed trait FailedConversionResult extends ConversionResult {
  def error: Exception
  def isFailure = true
}

case class NoConversionNeeded(model: Model) extends ConversionResult {
  def isFailure = false
}
case class SuccessfulConversion(originalModel: Model, model: Model) extends ConversionResult {
  def isFailure = false
}
case class ErroredConversion(model: Model, error: Exception) extends FailedConversionResult
case class ComponentConversionError(model: Model, error: Exception, componentName: String) extends FailedConversionResult
