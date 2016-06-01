// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, nvm }

object MiddleEnd extends MiddleEndInterface {

  // StructureParser found the top level Procedures for us.  ExpressionParser
  // finds command tasks and makes Procedures out of them, too.  the remaining
  // phases handle all ProcedureDefinitions from both sources. - ST 2/4/11
  def middleEnd(defs: Seq[ProcedureDefinition], source: String, flags: nvm.CompilerFlags): Seq[ProcedureDefinition] = {
    // lambda-lift
    val allDefs = {
      val taskNumbers = Iterator.from(1)
      defs.flatMap{procdef =>
        val lifter = new LambdaLifter(taskNumbers)
        procdef.accept(lifter)
        procdef +: lifter.children
      }
    }

    // each Int is the position of that variable in the procedure's args list
    val alteredLets =
      collection.mutable.Map[nvm.Procedure, collection.mutable.Map[core.Let, Int]]()

    for(procdef <- allDefs) {
      procdef.accept(new ReferenceVisitor)  // handle ReferenceType
      procdef.accept(new SourceTagger(source))
      // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
      procdef.accept(new SimpleOfVisitor)  // convert _of(_*variable) => _*variableof
      procdef.accept(new TaskVisitor)  // handle _taskvariable
      procdef.accept(new LocalsVisitor(alteredLets)) // convert _let/_repeat to _locals
      procdef.accept(new SetVisitor)   // convert _set to specific setters
    }

    if (flags.useOptimizer)
      for(procdef <- allDefs)
        procdef.accept(Optimizer)   // do various code-improving rewrites

    allDefs
  }

}
