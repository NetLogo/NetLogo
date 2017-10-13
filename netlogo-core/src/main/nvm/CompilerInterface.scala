// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  api.{ EditorCompiler, ExtensionManager => ApiExtensionManager, SourceOwner, Version, World },
  core.{ CompilerException, CompilationEnvironment, CompilerUtilitiesInterface, Dialect,
    FrontEndInterface, ProcedureSyntax, Program, Token }

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {
  import Procedure.ProceduresMap
  def frontEnd: FrontEndInterface
  def dialect: Dialect
  def utilities: CompilerUtilitiesInterface
  def compileProgram(source: String, program: Program, extensionManager: ApiExtensionManager,
    compilationEnvironment: CompilationEnvironment, flags: CompilerFlags): CompilerResults
  def compileMoreCode(source: String, displayName: Option[String], program: Program,
    oldProcedures: ProceduresMap, extensionManager: ApiExtensionManager, compilationEnvironment: CompilationEnvironment,
    flags: CompilerFlags): CompilerResults
  @throws(classOf[CompilerException])
  def compileProgram(source: String, additionalSources: Seq[SourceOwner], program: Program, extensionManager: ApiExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults
  def makeLiteralReporter(value: AnyRef): Reporter
}

trait AuxiliaryCompilerInterface extends EditorCompiler {
  def findIncludes(sourceFileName: String, source: String, environment: CompilationEnvironment): Option[Map[String, String]]

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ApiExtensionManager): AnyRef

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]

  def tokenizeForColorization(source: String, extensionManager: ApiExtensionManager): Array[Token]
  def tokenizeForColorizationIterator(source: String, extensionManager: ApiExtensionManager): Iterator[Token]
}

trait PresentationCompilerInterface extends CompilerInterface with AuxiliaryCompilerInterface

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = Version.useGenerator,
  useOptimizer: Boolean = Version.useOptimizer,
  optimizations: Optimizations.OptimizationList = Optimizations.empty)
