// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LiteralParser, Model }

import scala.util.Try

trait AutoConvertable {
  def requiresAutoConversion(original: Model, needsConversion: String => Boolean): Boolean = false
  def autoConvert(original: Model, autoConverter: AutoConverter): Try[Model] = Try(original)

  // ConversionSource should return any source needed to compile the entire model
  // Only used at the moment by System Dynamics, but could be used by other components
  // in the future.
  def conversionSource(m: Model, literalParser: LiteralParser): Option[(String, String)] = None
}

trait AutoConverter {
  def convertProcedure(procedure: String): String
  def convertStatement(statement: String): String
  def convertReporterProcedure(reporterProc: String): String
  def convertReporterExpression(expression: String): String
  def appliesToSource(source: String): Boolean
}
