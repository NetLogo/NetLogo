// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.nlogo.core.{Femto, Program}
import org.nlogo.{ api, nvm }

object BackEnd extends BackEndInterface {

  def backEnd(defs: Seq[ProcedureDefinition], program: Program,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults = {
    for(procdef <- defs) {
      if (flags.foldConstants)
        procdef.accept(new ConstantFolder)  // en.wikipedia.org/wiki/Constant_folding
      procdef.accept(new ArgumentStuffer) // fill args arrays in Commands & Reporters
      new Assembler().assemble(procdef)     // flatten tree to command array
      if (flags.useGenerator) // generate byte code
        procdef.procedure.code =
          Femto.get[nvm.GeneratorInterface]("org.nlogo.generate.Generator",
                    procdef.procedure, profilingEnabled)
            .generate()
    }
    // only return top level procedures.
    // task procedures can be reached via the children field on Procedure.
    nvm.CompilerResults(
      defs.map(_.procedure).filterNot(_.isTask),
      program)
  }

}
