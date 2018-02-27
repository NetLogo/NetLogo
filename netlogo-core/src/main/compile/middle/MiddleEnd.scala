// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, nvm }
import org.nlogo.core.{ CompilationEnvironment, Program }
import org.nlogo.compile.api.{ MiddleEndInterface, Optimizations, ProcedureDefinition }

object MiddleEnd extends MiddleEndInterface {

  // StructureParser found the top level Procedures for us.  ExpressionParser
  // finds anonymous commands and makes Procedures out of them, too.  the remaining
  // phases handle all ProcedureDefinitions from both sources. - ST 2/4/11
  def middleEnd(
    defs: Seq[ProcedureDefinition],
    program: Program,
    sources: Map[String, String],
    compilationEnvironment: CompilationEnvironment,
    optimizations: Optimizations): Seq[ProcedureDefinition] = {
    // lambda-lift
    val allDefs = {
      val taskNumbers = Iterator.from(1)
      defs.flatMap { procdef =>
        val lifter = new LambdaLifter(taskNumbers)
        val newProc = lifter.visitProcedureDefinition(procdef)
        newProc +: lifter.children
      }
    }

    // each Int is the position of that variable in the procedure's args list
    val alteredLets =
      collection.mutable.Map[nvm.Procedure, collection.mutable.Map[core.Let, Int]]()

    val transformedProcedures = allDefs.map(transformProcedure)

    val sourceTagger = new SourceTagger(sources, compilationEnvironment)
    val setVisitor   = new SetVisitor(program)

    for(procdef <- transformedProcedures) {
      procdef.accept(sourceTagger)
      // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
      procdef.accept(new SimpleOfVisitor)            // convert _of(_*variable) => _*variableof
      procdef.accept(new LambdaVariableVisitor)      // handle _lambdavariable
      procdef.accept(new LocalsVisitor(alteredLets)) // convert _let/_repeat to _locals
      procdef.accept(new RepeatVisitor)              // convert _repeat to use local variable
      procdef.accept(setVisitor)                     // convert _set to specific setters
    }

    if (optimizations.nonEmpty)
      for(procdef <- transformedProcedures)
        procdef.accept(new Optimizer(optimizations))   // do various code-improving rewrites

    transformedProcedures
  }

  private def transformProcedure(procdef: ProcedureDefinition): ProcedureDefinition = {
    val transformer = new ReferenceTransformer // handle ReferenceType
    val scopeTransformer = new ScopeTransformer // Adjust scope for repeated constructs
    scopeTransformer.visitProcedureDefinition(transformer.visitProcedureDefinition(procdef))
  }
}
