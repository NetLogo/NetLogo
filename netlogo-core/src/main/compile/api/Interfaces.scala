// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.{ CompilationEnvironment, StructureResults, Program }
import org.nlogo.{ core, nvm }

import scala.collection.immutable.ListMap

trait FrontMiddleBridgeInterface {
  def apply(
    structureResults: StructureResults,
    oldProcedures:    ListMap[Tuple2[String, Option[String]], nvm.Procedure],
    topLevelDefs:     Seq[core.ProcedureDefinition],
    backifier:        Backifier
  ): Seq[ProcedureDefinition]
}

trait MiddleEndInterface {
  def middleEnd(
    defs:          Seq[ProcedureDefinition],
    program:       Program,
    sources:       Map[String, String],
    environment:   CompilationEnvironment,
    optimizations: Optimizations): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: Program,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults

  def assemble(procDef: ProcedureDefinition, useGenerator: Boolean, profilingEnabled: Boolean): Unit
}
