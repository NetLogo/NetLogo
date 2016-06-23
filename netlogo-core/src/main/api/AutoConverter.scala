// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Model

trait AutoConvertable {
  def requiresAutoConversion(original: Model, needsConversion: String => Boolean): Boolean = false
  def autoConvert(original: Model, autoConverter: AutoConverter): Model = original
}

trait AutoConverter {
  def convertProcedure(procedure: String): String
  def convertStatement(statement: String): String
  def convertReporterProcedure(reporterProc: String): String
  def convertReporterExpression(expression: String): String
  def appliesToSource(source: String): Boolean
}
