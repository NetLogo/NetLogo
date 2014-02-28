// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// has to be in this package because that's where ProcedureDefinition is - ST 8/27/13

import org.nlogo.{ api, nvm },
  nvm.FrontEndInterface.ProceduresMap

trait FrontEndInterface extends nvm.FrontEndInterface {
  def frontEnd(source: String, oldProcedures: ProceduresMap = nvm.FrontEndInterface.NoProcedures,
      program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], nvm.StructureResults)
  def frontEndHelper(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager)
    : (Seq[ProcedureDefinition], nvm.StructureResults)
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], flags: nvm.CompilerFlags): Unit
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: api.Program, source: String,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
