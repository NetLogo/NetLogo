// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ CompilationEnvironment, CompilerException,
  CompilerUtilitiesInterface, Dialect, ProcedureSyntax, Program, Token }
import org.nlogo.api.{ ExtensionManager => ApiExtensionManager, SourceOwner, Version, World }

import scala.collection.immutable.ListMap

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {

  def defaultDialect: Dialect

  def utilities: CompilerUtilitiesInterface

  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def compileProgram(source: String, additionalSources: Seq[SourceOwner], program: Program, extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: ListMap[String, Procedure],
                      extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: ListMap[String, Procedure],
                         extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: ListMap[String, Procedure],
                          extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ApiExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ApiExtensionManager): java.lang.Double

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: ApiExtensionManager): AnyRef

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]
  def findIncludes(sourceFileName: String, source: String, environment: CompilationEnvironment): Option[Map[String, String]]
  def isValidIdentifier(s: String): Boolean
  def isReporter(s: String, program: Program, procedures: ListMap[String, Procedure], extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ApiExtensionManager): Array[Token]
  def tokenizeForColorizationIterator(source: String, extensionManager: ApiExtensionManager): Iterator[Token]
}

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = Version.useGenerator,
  useOptimizer: Boolean = Version.useOptimizer,
  optimizations: Seq[String] = Seq.empty[String])
