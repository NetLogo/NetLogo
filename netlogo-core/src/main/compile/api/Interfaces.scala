// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.{ StructureResults, Program }
import org.nlogo.{ core, api => nlogoApi, nvm },
  nvm.Procedure.ProceduresMap

trait FrontMiddleBridgeInterface {
  def apply(
    structureResults: StructureResults,
    oldProcedures:    ProceduresMap,
    topLevelDefs:     Seq[core.ProcedureDefinition],
    backifier:        Backifier
  ): Seq[ProcedureDefinition]
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], sources: Map[String, String], optimizations: Optimizations): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: Program,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
