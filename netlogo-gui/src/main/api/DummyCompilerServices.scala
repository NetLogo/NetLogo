// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilerException, ProcedureSyntax, Token }

// just enough functionality to make the tests pass

class DummyCompilerServices extends CompilerServices {
  private def unsupported = throw new UnsupportedOperationException
  def readFromString(s: String): AnyRef =
    try { s.toDouble: java.lang.Double }
    catch {
      case ex: NumberFormatException =>
        s match {
          case "true" => true: java.lang.Boolean
          case "false" => false: java.lang.Boolean
          case _ => throw new CompilerException(
            "not a constant recognized by DummyCompilerServices", 0, s.size, "")
        }
    }
  def autoConvert(modelVersion: String)(source: String) = source
  def readNumberFromString(source: String) = source
  def checkReporterSyntax(source: String) { }
  def checkCommandSyntax(source: String) { }
  def isConstant(s: String): Boolean = unsupported
  def isValidIdentifier(s: String): Boolean = unsupported
  def isReporter(s: String): Boolean = unsupported
  def tokenizeForColorization(s: String): Array[Token] = unsupported
  def getTokenAtPosition(source: String, position: Int): Token = unsupported
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax] = unsupported
}
