// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

import org.nlogo.api
import api.{ ExtensionManager, Program }
import org.nlogo.nvm.{ CompilerFlags, CompilerResults, GeneratorInterface, Procedure }
import org.nlogo.util.Femto
import org.nlogo.parse

private object BackEnd {

  // StructureParser found the top level Procedures for us.  ExpressionParser
  // finds command tasks and makes Procedures out of them, too.  the remaining
  // phases handle all ProcedureDefinitions from both sources. - ST 2/4/11
  def backEnd(defs: Seq[parse.ProcedureDefinition], structureResults: parse.StructureParser.Results, source: String, profilingEnabled: Boolean, flags: CompilerFlags): CompilerResults = {
    for(procdef <- defs) {
      procdef.accept(new ReferenceVisitor)  // handle ReferenceType
      if (flags.foldConstants)
        procdef.accept(new ConstantFolder)  // en.wikipedia.org/wiki/Constant_folding
      // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
      procdef.accept(new SimpleOfVisitor)  // convert _of(_*variable) => _*variableof
      procdef.accept(new TaskVisitor)  // handle _reportertask
      procdef.accept(new LocalsVisitor)  // convert _let/_repeat to _locals
      procdef.accept(new parse.SetVisitor)   // convert _set to specific setters
      procdef.accept(new CarefullyVisitor)  // connect _carefully to _errormessage
      if (flags.useOptimizer)
        procdef.accept(Optimizer)   // do various code-improving rewrites
    }
    new AgentTypeChecker(defs).check()  // catch agent type inconsistencies
    for(procdef <- defs) {
      procdef.accept(new ArgumentStuffer) // fill args arrays in Commands & Reporters
      new Assembler().assemble(procdef)     // flatten tree to command array
      if (flags.useGenerator) // generate byte code
        procdef.procedure.code =
          Femto.get(classOf[GeneratorInterface], "org.nlogo.generate.Generator",
                    Array(source, procdef.procedure,
                          Boolean.box(
                            profilingEnabled)))
            .generate()
    }
    // only return top level procedures.
    // task procedures can be reached via the children field on Procedure.
    CompilerResults(
      defs.map(_.procedure).filterNot(_.isTask),
      structureResults.program)
  }

}
