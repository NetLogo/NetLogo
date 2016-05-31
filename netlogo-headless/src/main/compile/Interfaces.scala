// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{StructureResults, Program}
import org.nlogo.{ core, api, nvm },
  nvm.Procedure.ProceduresMap

trait FrontMiddleBridgeInterface {
  def apply(
    structureResults: StructureResults,
    extensionManager: api.ExtensionManager,
    oldProcedures: ProceduresMap,
    topLevelDefs: Seq[core.ProcedureDefinition]
  ): Seq[ProcedureDefinition]
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], source: String, flags: nvm.CompilerFlags): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: Program,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
