// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilerException, Dialect, FrontEndProcedure, ProcedureSyntax, Program, Token }

import scala.collection.immutable.ListMap

private[nlogo] trait EditorCompiler {
  def defaultDialect: Dialect

  def isValidIdentifier(s: String): Boolean

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: ListMap[String, FrontEndProcedure],
                         extensionManager: ExtensionManager, parse: Boolean)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: ListMap[String, FrontEndProcedure],
                          extensionManager: ExtensionManager, parse: Boolean)

  def getTokenAtPosition(source: String, position: Int): Token

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): java.lang.Double

  def isReporter(s: String, program: Program, procedures: ListMap[String, FrontEndProcedure],
                extensionManager: ExtensionManager): Boolean

  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token]
  def tokenizeForColorizationIterator(source: String, extensionManager: ExtensionManager): Iterator[Token]

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]
}
