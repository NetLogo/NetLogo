// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api => nlogoApi, core, nvm },
  core.{ CompilationEnvironment, Femto, CompilerUtilitiesInterface, FrontEndInterface, Program},
  nvm.Procedure.{ ProceduresMap, NoProcedures }

import org.nlogo.compile.api.{ Backifier => BackifierInterface, BackEndInterface,
  CommandMunger, FrontMiddleBridgeInterface, MiddleEndInterface, Optimizations, ReporterMunger }

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

object Compiler extends nvm.CompilerInterface {

  val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")
  val utilities = Femto.scalaSingleton[CompilerUtilitiesInterface](
    "org.nlogo.parse.CompilerUtilities")
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
    val allDefs = middleEnd.middleEnd(bridged, Map("" -> source), compilationEnvironment, getOptimizations(flags))
    backEnd.backEnd(allDefs, structureResults.program, compilationEnvironment.profilingEnabled, flags)
  }

  private def getOptimizations(flags: nvm.CompilerFlags): Optimizations =
    if (flags.useOptimizer) {
      val commandOpt =
        Seq("Fd1", "FdLessThan1", "HatchFast", "SproutFast", "CrtFast", "CroFast")
          .map(opt => s"org.nlogo.compile.middle.optimize.$opt")
          .map(className => Femto.scalaSingleton[CommandMunger](className))
      val reporterOpt =
        Seq("PatchAt", "With", "OneOfWith", "Nsum", "Nsum4", "CountWith", "OtherWith",
          "WithOther", "AnyOther", "AnyOtherWith", "CountOther", "CountOtherWith", "AnyWith1",
          "AnyWith2", "AnyWith3", "AnyWith4", "AnyWith5", "PatchVariableDouble",
          "TurtleVariableDouble", "RandomConst")
          .map(opt => s"org.nlogo.compile.middle.optimize.$opt")
          .map(className => Femto.scalaSingleton[ReporterMunger](className))
      val headlessSpecificOptimizations = Seq("Constants", "InRadiusBoundingBox")
        .map(opt => s"org.nlogo.compile.optimize.$opt")
        .map(className => Femto.scalaSingleton[ReporterMunger](className))
      Optimizations(commandOpt, reporterOpt ++ headlessSpecificOptimizations)
    } else
      Optimizations.none

  def makeLiteralReporter(value: AnyRef): nvm.Reporter =
    Literals.makeLiteralReporter(value)
}
