// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected in
// StructureParser. - ST 2/21/08, 1/21/09

import org.nlogo.api.{ ExtensionManager, Version }
import org.nlogo.compile.api.{ Backifier => BackifierInterface, CommandMunger, DefaultAstVisitor,
  FrontMiddleBridgeInterface, MiddleEndInterface, Optimizations, ProcedureDefinition, ReporterMunger }
import org.nlogo.core.{ Dialect, Program }
import org.nlogo.nvm.{ CompilerFlags, GeneratorInterface, Optimizations => NvmOptimizations, Procedure }
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, FrontEndInterface, Femto }
import scala.collection.immutable.ListMap

private[compile] object CompilerMain {

  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")
  val middleEnd = Femto.scalaSingleton[MiddleEndInterface](
    "org.nlogo.compile.middle.MiddleEnd")

  def backifier(program: Program, extensionManager: ExtensionManager): BackifierInterface =
    new Backifier(program, extensionManager)

  private val frontEnd =
    Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

  def compile(
    sources:          Map[String, String],
    displayName:      Option[String],
    program:          Program,
    subprogram:       Boolean,
    oldProcedures:    ListMap[String, Procedure],
    extensionManager: ExtensionManager,
    compilationEnv:   CompilationEnvironment): (ListMap[String, Procedure], Program) = {

    val oldProceduresListMap = ListMap[String, Procedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(CompilationOperand(sources, extensionManager, compilationEnv, program, oldProceduresListMap, subprogram, displayName))

    val bridged = bridge(feStructureResults, oldProcedures, topLevelDefs, backifier(feStructureResults.program, extensionManager))

    // NOTE: This only provides a list of optimizations to run.
    // The optimization system property (in api.Version) controls
    // whether those are actually turned on.
    val flags = CompilerFlags(optimizations =
      if (program.dialect.is3D) NvmOptimizations.gui3DOptimizations
      else NvmOptimizations.guiOptimizations)

    val allDefs = middleEnd.middleEnd(
      bridged,
      feStructureResults.program,
      sources,
      compilationEnv,
      getOptimizations(flags, feStructureResults.program.dialect))

    val newProcedures =
      allDefs
        .map(assembleProcedure(_, feStructureResults.program, compilationEnv))
        .filterNot(_.isLambda)
        .map(p => p.name -> p)

    val returnedProcedures = ListMap(newProcedures: _*) ++ oldProcedures
    // only return top level procedures.
    // anonymous procedures can be reached via the children field on Procedure.
    (returnedProcedures, feStructureResults.program)
  }

  // These phases optimize and tweak the ProcedureDefinitions given by frontEnd/CompilerBridge. - RG 10/29/15
  // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
  def assembleProcedure(procdef: ProcedureDefinition, program: Program, compilationEnv: CompilationEnvironment): Procedure = {
    val optimizers: Seq[DefaultAstVisitor] = Seq(
      new ConstantFolder, // en.wikipedia.org/wiki/Constant_folding
      new ArgumentStuffer // fill args arrays in Commands & Reporters
    )
    for (optimizer <- optimizers) {
      procdef.accept(optimizer)
    }
    new Assembler().assemble(procdef) // flatten tree to command array
    if(Version.useGenerator) // generate byte code
      procdef.procedure.code =
        Femto.get[GeneratorInterface]("org.nlogo.generate.Generator", procdef.procedure,
          Boolean.box(compilationEnv.profilingEnabled)).generate()

    procdef.procedure
  }

  private def getOptimizations(flags: CompilerFlags, dialect: Dialect): Optimizations =
    if (flags.useOptimizer)
      flags.optimizations.foldLeft(Optimizations.none) {
        case (opts, (NvmOptimizations.Reporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.scalaSingleton[ReporterMunger](klass) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.DialectReporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.get[ReporterMunger](klass, dialect) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.Command, klass)) =>
          opts.copy(commandOptimizations = Femto.scalaSingleton[CommandMunger](klass) +: opts.commandOptimizations)
      }
    else
      Optimizations.none
}
