// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected in
// StructureParser. - ST 2/21/08, 1/21/09

import org.nlogo.api.Version
import org.nlogo.core.Program
import org.nlogo.nvm.{GeneratorInterface, Procedure}
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, ExtensionManager, FrontEndInterface, FrontEndProcedure, Femto }
import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._

private object CompilerMain {

  private val frontEnd =
    Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

  def compile(sources: Map[String, String], displayName: Option[String], program: Program, subprogram: Boolean,
              oldProcedures: java.util.Map[String, Procedure],
              extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): (Seq[Procedure], Program) = {

    val oldProceduresListMap = ListMap[String, Procedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(CompilationOperand(sources, extensionManager, compilationEnv, program, oldProceduresListMap, subprogram, displayName))

    val defs = CompilerBridge(feStructureResults, extensionManager, oldProceduresListMap, topLevelDefs)

    val allSources = sources ++
      feStructureResults.includedSources.map(i =>
          (i -> compilationEnv.getSource(compilationEnv.resolvePath(i))))

    val returnedProcedures =
      defs.map(assembleProcedure(_, feStructureResults.program, allSources, compilationEnv))
      .filterNot(_.isTask) ++ oldProcedures.values

    // only return top level procedures.
    // task procedures can be reached via the children field on Procedure.
    (returnedProcedures, feStructureResults.program)
  }

  // These phases optimize and tweak the ProcedureDefinitions given by frontEnd/CompilerBridge. - RG 10/29/15
  // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
  def assembleProcedure(procdef: ProcedureDefinition, program: Program, sources: Map[String, String], compilationEnv: CompilationEnvironment): Procedure = {
    val optimizers: Seq[DefaultAstVisitor] = Seq(
      new ReferenceVisitor, // handle ReferenceType
      new SourceTagger(sources),
      new ConstantFolder, // en.wikipedia.org/wiki/Constant_folding
      new SimpleOfVisitor, // convert _of(_*variable) => _*variableof
      new TaskVisitor, // handle _reportertask
      new LocalsVisitor, // convert _let/_repeat to _locals
      new SetVisitor,  // convert _set to specific setters
      new Optimizer(program.dialect.is3D),  // do various code-improving rewrites
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
}
