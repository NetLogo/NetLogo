// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api => nlogoApi, core, nvm },
  nlogoApi.{ ExtensionManager, SourceOwner },
  core.{ CompilationEnvironment, CompilationOperand, Femto, FrontEndInterface, NetLogoCore, Program },
  nvm.{ CompilerFlags, CompilerResults, Optimizations => NvmOptimizations, PresentationCompilerInterface, Procedure },
    Procedure.{ ProceduresMap, NoProcedures }

import org.nlogo.compile.api.{ BackEndInterface, CommandMunger, FrontMiddleBridgeInterface,
  MiddleEndInterface, Optimizations, PresentationCompiler, ReporterMunger }

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

object Compiler extends PresentationCompilerInterface with PresentationCompiler {

  val dialect = NetLogoCore
  val defaultDialect = dialect

  val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")
  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")
  val middleEnd = Femto.scalaSingleton[MiddleEndInterface](
    "org.nlogo.compile.middle.MiddleEnd")
  val backEnd = Femto.scalaSingleton[BackEndInterface](
    "org.nlogo.compile.back.BackEnd")
  def backifier(program: Program, extensionManager: nlogoApi.ExtensionManager) =
    new Backifier(program, extensionManager)

  // used to compile the Code tab, including declarations
  def compileProgram(source: String, program: Program, extensionManager: nlogoApi.ExtensionManager,
    compilationEnvironment: CompilationEnvironment, flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, None, program, false, NoProcedures, extensionManager, compilationEnvironment, flags)

  // used to compile a single procedures only, from outside the Code tab
  def compileMoreCode(source: String, displayName: Option[String], program: Program,
      oldProcedures: ProceduresMap, extensionManager: nlogoApi.ExtensionManager,
      compilationEnvironment: CompilationEnvironment, flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, displayName, program, true, oldProcedures, extensionManager, compilationEnvironment, flags)

  private def compile(source: String, displayName: Option[String], oldProgram: Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: nlogoApi.ExtensionManager,
      compilationEnvironment: CompilationEnvironment, flags: nvm.CompilerFlags): nvm.CompilerResults = {
    val (topLevelDefs, structureResults) =
      frontEnd.frontEnd(source, displayName, oldProgram, subprogram, oldProcedures, extensionManager, compilationEnvironment)
    val bridged = bridge(structureResults, oldProcedures, topLevelDefs, backifier(structureResults.program, extensionManager))
    val allDefs = middleEnd.middleEnd(
      bridged,
      structureResults.program,
      Map("" -> source),
      compilationEnvironment,
      getOptimizations(flags))
    backEnd.backEnd(allDefs, structureResults.program, compilationEnvironment.profilingEnabled, flags)
  }

  val defaultCompilerFlags =
    CompilerFlags(optimizations = NvmOptimizations.headlessOptimizations)

  def compileProgram(source: String, additionalSources: Seq[SourceOwner], program: Program, extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults = {
    val allSources =
      Map("" -> source) ++ additionalSources.map(additionalSource => additionalSource.classDisplayName -> additionalSource.innerSource).toMap
    val (topLevelDefs, structureResults) =
      frontEnd.frontEnd(CompilationOperand(allSources, extensionManager, compilationEnv, program, Procedure.NoProcedures, subprogram = false))
    val bridged = bridge(structureResults, Procedure.NoProcedures, topLevelDefs, backifier(structureResults.program, extensionManager))
    val allDefs = middleEnd.middleEnd(
      bridged,
      structureResults.program,
      allSources,
      compilationEnv,
      getOptimizations(defaultCompilerFlags))
    backEnd.backEnd(allDefs, structureResults.program, compilationEnv.profilingEnabled, defaultCompilerFlags)
  }

  private def getOptimizations(flags: nvm.CompilerFlags): Optimizations =
    if (flags.useOptimizer)
      flags.optimizations.foldLeft(Optimizations.none) {
        case (opts, (NvmOptimizations.Reporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.scalaSingleton[ReporterMunger](klass) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.DialectReporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.get[ReporterMunger](klass, NetLogoCore) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.Command, klass)) =>
          opts.copy(commandOptimizations = Femto.scalaSingleton[CommandMunger](klass) +: opts.commandOptimizations)
      }
    else
      Optimizations.none

  def makeLiteralReporter(value: AnyRef): nvm.Reporter =
    Literals.makeLiteralReporter(value)
}
