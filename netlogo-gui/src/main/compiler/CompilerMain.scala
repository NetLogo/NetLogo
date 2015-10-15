// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected in
// StructureParser. - ST 2/21/08, 1/21/09

import org.nlogo.api.{ Version }
import org.nlogo.core.Program
import org.nlogo.nvm.{GeneratorInterface, Procedure}
import org.nlogo.core.CompilationEnvironment
import org.nlogo.core.{ ExtensionManager, FrontEndInterface, FrontEndProcedure, Femto }
import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._

private object CompilerMain {

  // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
  private val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")

  def compile(source: String, displayName: Option[String], program: Program, subprogram: Boolean,
              oldProcedures: java.util.Map[String, Procedure],
              extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): (Seq[Procedure], Program) = {

    val oldProgram = program.copy()

    implicit val tokenizer = if(program.dialect.is3D) Compiler.Tokenizer3D else Compiler.Tokenizer2D
    val structureResults = new StructureParser(tokenizer.tokenize(source), // tokenize
                                               displayName, program, oldProcedures, extensionManager, compilationEnv)
      .parse(subprogram)  // process declarations
    val defs = new collection.mutable.ArrayBuffer[ProcedureDefinition]
    import collection.JavaConverters._  // structureResults.procedures.values is a java.util.Collection
    val taskNumbers = Iterator.from(1)
    for(procedure <- structureResults.procedures.values.asScala) {
      procedure.topLevel = subprogram
      val tokens =
        new IdentifierParser(structureResults.program, oldProcedures, structureResults.procedures)
        .process(structureResults.tokens(procedure).iterator, procedure)  // resolve references
      defs ++= new ExpressionParser(procedure, taskNumbers).parse(tokens) // parse
    }

    val oldProceduresListMap =
      ListMap[String, FrontEndProcedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, displayName, oldProgram, subprogram, oldProceduresListMap, extensionManager)

    // StructureParser found the top level Procedures for us.  ExpressionParser
    // finds command tasks and makes Procedures out of them, too.  the remaining
    // phases handle all ProcedureDefinitions from both sources. - ST 2/4/11
    for(procdef <- defs) {
      procdef.accept(new ReferenceVisitor)  // handle ReferenceType
      procdef.accept(new ConstantFolder)  // en.wikipedia.org/wiki/Constant_folding
      procdef.accept(new SimpleOfVisitor)  // convert _of(_*variable) => _*variableof
      procdef.accept(new TaskVisitor)  // handle _reportertask
      procdef.accept(new LocalsVisitor)  // convert _let/_repeat to _locals
      procdef.accept(new SetVisitor)   // convert _set to specific setters
      procdef.accept(new CarefullyVisitor)  // connect _carefully to _errormessage
      procdef.accept(new Optimizer(structureResults.program.dialect.is3D))   // do various code-improving rewrites
    }
    new AgentTypeChecker(defs).parse()  // catch agent type inconsistencies
    for(procdef <- defs) {
      procdef.accept(new ArgumentStuffer) // fill args arrays in Commands & Reporters
      new Assembler().assemble(procdef)     // flatten tree to command array
      if(Version.useGenerator) // generate byte code
        procdef.procedure.code =
          Femto.get[GeneratorInterface]("org.nlogo.generator.Generator",
                    source, procdef.procedure,
                          Boolean.box(
                            compilationEnv.profilingEnabled)).generate()
    }
    // only return top level procedures.
    // task procedures can be reached via the children field on Procedure.
    (defs.map(_.procedure).filterNot(_.isTask), structureResults.program)
  }
}
