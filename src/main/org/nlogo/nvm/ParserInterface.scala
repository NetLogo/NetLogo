// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

import org.nlogo.api, api.{ Program, ExtensionManager, Token, World }
import CompilerInterface.ProceduresMap

trait ParserInterface {
  def checkCommandSyntax(source: String, program: Program, procedures: ProceduresMap,
                         extensionManager: ExtensionManager, parse: Boolean)
  def checkReporterSyntax(source: String, program: Program, procedures: ProceduresMap,
                          extensionManager: ExtensionManager, parse: Boolean)
  def readFromString(source: String): AnyRef
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef
  def findProcedurePositions(source: String): Map[String, (String, Int, Int, Int)]
  def findIncludes(sourceFileName: String, source: String): Map[String, String]
  def isValidIdentifier(s: String): Boolean
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Seq[Token]
}
