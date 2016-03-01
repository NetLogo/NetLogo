// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.{FrontEndProcedure, StructureResults}
import org.nlogo.{ core, api, nvm },
  nvm.Procedure.ProceduresMap

object FrontMiddleBridge extends FrontMiddleBridgeInterface {
  def apply(
    structureResults: StructureResults,
    extensionManager: api.ExtensionManager,
    oldProcedures: ProceduresMap,
    topLevelDefs: Seq[core.ProcedureDefinition]
  ): Seq[ProcedureDefinition] = {
    val newProcedures = structureResults.procedures.map {
      case (k, p) => k -> fromApiProcedure(p)
    }.toMap
    // mapValues won't work here because we need identity preservation across all uses of newProcedures
    // assert(newProcedures.forall { case (k, p) => newProcedures(k) eq p })
    val backifier = new middle.Backifier(
      structureResults.program, extensionManager, oldProcedures ++ newProcedures)
    val astBackifier = new middle.ASTBackifier(backifier)
    (newProcedures.values, topLevelDefs)
      .zipped
      .map(astBackifier.backify)
      .toSeq
  }
  private def fromApiProcedure(p: FrontEndProcedure): nvm.Procedure = {
    val proc = new nvm.Procedure(
      isReporter = p.isReporter,
      name = p.name,
      nameToken = p.nameToken,
      argTokens = p.argTokens,
      _displayName = if (p.displayName == "") None else Some(p.displayName),
      procedureDeclaration = p.procedureDeclaration
    )
    proc.agentClassString = p.agentClassString
    proc.topLevel = p.topLevel
    proc.args = p.args
    proc
  }
}
