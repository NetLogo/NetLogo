// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ CompilationEnvironment, CompilerUtilitiesInterface, Dialect }
import org.nlogo.api.World
import org.nlogo.core.{ CompilerException, ProcedureSyntax, Program, Token }
import org.nlogo.api.{ ExtensionManager => ApiExtensionManager, SourceOwner }

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {

  def defaultDialect: Dialect

  def compilerUtilities: CompilerUtilitiesInterface

  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def compileProgram(source: String, additionalSources: Seq[SourceOwner], program: Program, extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: java.util.Map[String, Procedure],
                      extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                         extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                          extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  def autoConvert(version: String)(source: String): String

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ApiExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ApiExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: ApiExtensionManager): AnyRef

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]
  def findIncludes(sourceFileName: String, source: String, environment: CompilationEnvironment): Option[Map[String, String]]
  def isValidIdentifier(s: String): Boolean
  def isReporter(s: String, program: Program, procedures: java.util.Map[String, Procedure], extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ApiExtensionManager): Array[Token]
}
