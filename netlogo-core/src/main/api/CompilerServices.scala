// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilerException, Dialect, LiteralParser, ProcedureSyntax, Token }

trait CompilerServices extends LiteralParser {
  def dialect: Dialect
  def isConstant(s: String): Boolean
  @throws(classOf[CompilerException])
  def readNumberFromString(source: String): java.lang.Double
  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String): Unit
  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String): Unit
  def isReporter(s: String): Boolean
  def isValidIdentifier(s: String): Boolean
  def tokenizeForColorization(source: String): Array[Token]
  def tokenizeForColorizationIterator(source: String): Iterator[Token]
  def getTokenAtPosition(source: String, position: Int): Token
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]
}
