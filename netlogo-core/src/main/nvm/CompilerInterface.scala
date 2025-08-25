// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  api.{ ExtensionManager => ApiExtensionManager, LibraryManager, SourceOwner, Version, World },
  core.{ CompilerException, CompilationEnvironment, CompilerUtilitiesInterface, Dialect, FrontEndInterface, ProcedureSyntax,
    Program, Token }

import scala.collection.immutable.ListMap

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {
  import Procedure.ProceduresMap
  def frontEnd: FrontEndInterface
  def utilities: CompilerUtilitiesInterface
  def compileProgram(source: String, program: Program, extensionManager: ApiExtensionManager, libManager: LibraryManager,
    compilationEnvironment: CompilationEnvironment, shouldAutoInstallLibs: Boolean,
    flags: CompilerFlags = CompilerFlags()): CompilerResults
  def compileMoreCode(source: String, displayName: Option[String], program: Program
  , oldProcedures: ProceduresMap, extensionManager: ApiExtensionManager, libManager: LibraryManager
  , compilationEnvironment: CompilationEnvironment
  , flags: CompilerFlags = CompilerFlags()): CompilerResults
  @throws(classOf[CompilerException])
  def compileProgram( source: String, additionalSources: Seq[SourceOwner], program: Program
                    , extensionManager: ApiExtensionManager, libManager: LibraryManager
                    , compilationEnv: CompilationEnvironment, shouldAutoInstallLibs: Boolean): CompilerResults
  def makeLiteralReporter(value: AnyRef): Reporter
}

trait AuxiliaryCompilerInterface {
  def defaultDialect: Dialect

  def findIncludes(sourceFileName: String, source: String, environment: CompilationEnvironment): Option[Map[String, String]]

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef
  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ApiExtensionManager): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ApiExtensionManager): java.lang.Double

  def getTokenAtPosition(source: String, position: Int): Token

  def isValidIdentifier(s: String, extensionManager: ApiExtensionManager): Boolean
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]

  def isReporter(s: String, program: Program, procedures: ListMap[(String, Option[String]), Procedure], extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): Boolean

  def tokenizeForColorization(source: String, extensionManager: ApiExtensionManager): Array[Token]
  def tokenizeForColorizationIterator(source: String, extensionManager: ApiExtensionManager): Iterator[Token]

  def tokenizeWithWhitespace(source: String, extensionManager: ApiExtensionManager): Iterator[Token]

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: ListMap[(String, Option[String]), Procedure],
                         extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment): Unit

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: ListMap[(String, Option[String]), Procedure],
                          extensionManager: ApiExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment): Unit
}

trait PresentationCompilerInterface extends CompilerInterface with AuxiliaryCompilerInterface

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = Version.useGenerator,
  useOptimizer: Boolean = Version.useOptimizer,
  optimizations: Optimizations.OptimizationList = Optimizations.empty)
