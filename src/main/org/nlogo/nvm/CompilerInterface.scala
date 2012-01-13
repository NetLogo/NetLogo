// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{Program, World, CompilerException, ExtensionManager, Token}

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {

  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager): CompilerResults

  @throws(classOf[CompilerException])
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: java.util.Map[String, Procedure],
                      extensionManager: ExtensionManager): CompilerResults

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                         extensionManager: ExtensionManager, parse: Boolean)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                          extensionManager: ExtensionManager, parse: Boolean)

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef

  def findProcedurePositions(source: String): java.util.Map[String, java.util.List[AnyRef]]
  def findIncludes(sourceFileName: String, source: String): java.util.Map[String, String]
  def isValidIdentifier(s: String): Boolean
  def isReporter(s: String, program: Program, procedures: java.util.Map[String, Procedure], extensionManager: ExtensionManager): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token]
}
