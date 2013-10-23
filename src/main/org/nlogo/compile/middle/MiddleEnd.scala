// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ api, nvm }

object MiddleEnd extends MiddleEndInterface {

  // StructureParser found the top level Procedures for us.  ExpressionParser
  // finds command tasks and makes Procedures out of them, too.  the remaining
  // phases handle all ProcedureDefinitions from both sources. - ST 2/4/11
  def middleEnd(defs: Seq[ProcedureDefinition], flags: nvm.CompilerFlags) {
    // each Int is the position of that variable in the procedure's args list
    val alteredLets =
      collection.mutable.Map[nvm.Procedure, collection.mutable.Map[api.Let, Int]]()
    for(procdef <- defs) {
      procdef.accept(new ReferenceVisitor)  // handle ReferenceType
      // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
      procdef.accept(new SimpleOfVisitor)  // convert _of(_*variable) => _*variableof
      procdef.accept(new TaskVisitor)  // handle _reportertask
      procdef.accept(new LocalsVisitor(alteredLets)) // convert _let/_repeat to _locals
      procdef.accept(new SetVisitor)   // convert _set to specific setters
      procdef.accept(new CarefullyVisitor)  // connect _carefully to _errormessage
      if (flags.useOptimizer)
        procdef.accept(Optimizer)   // do various code-improving rewrites
    }
    new AgentTypeChecker(defs).check()  // catch agent type inconsistencies
  }

}
