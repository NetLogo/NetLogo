// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LiteralParser, Model }

import scala.util.Try

trait AutoConvertable {
  def requiresAutoConversion(original: Model, needsConversion: String => Boolean): Boolean = false

  /** Converts the component and returns the updated model
   *
   *  Returns an either which encapsulates the result of the conversion process.
   *  A left indicates that something went wrong and includes both the model, assumed to have
   *  undergone a best-effort conversion, and exceptions indicating failures encountered
   *  in conversion.
   *
   *  For instance, if a widget errors during conversion, the widget converter converts and
   *  returns a `Left` containing the model with the other widgets converted
   *  and the erroring widget unconverted, as well as the exceptions when converting the widgets
   *
   *  @return Either a partially-converted model and exceptions, or a fully-converted model
   */
  def autoConvert(original: Model, autoConverter: AutoConverter): Either[(Model, Seq[Exception]), Model] = Right(original)

  // ConversionSource should return any source needed to compile the entire model
  // Only used at the moment by System Dynamics, but could be used by other components
  // in the future.
  def conversionSource(m: Model, literalParser: LiteralParser): Option[(String, String)] = None

  // A human-readable description of what this converts, for use in error messages
  def componentDescription: String
}

trait AutoConverter {
  def convertProcedure(procedure: String): String
  def convertStatement(statement: String): String
  def convertReporterProcedure(reporterProc: String): String
  def convertReporterExpression(expression: String): String
  def appliesToSource(source: String): Boolean
}
