// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.{FrontEndProcedure, StructureResults}
import org.nlogo.{ core, nvm },
  nvm.Procedure
import org.nlogo.compile.api.{ Backifier => ApiBackifier, FrontMiddleBridgeInterface, ProcedureDefinition }
import scala.collection.immutable.ListMap

object FrontMiddleBridge extends FrontMiddleBridgeInterface {
  def apply(
    structureResults: StructureResults,
    oldProcedures:    ListMap[Tuple2[String, Option[String]], nvm.Procedure],
    topLevelDefs:     Seq[core.ProcedureDefinition],
    backifier:        ApiBackifier
  ): Seq[ProcedureDefinition] = {
    // mapValues won't work here because we need identity preservation across all uses of newProcedures
    // assert(newProcedures.forall { case (k, p) => newProcedures(k) eq p })
    val newProcedures = structureResults.procedures.map {
      case (k, p) => k -> fromApiProcedure(p)
    }.toMap
    val astBackifier = new middle.ASTBackifier(backifier, ListMap((newProcedures ++ oldProcedures).toSeq*))
    newProcedures
      .filter{case ((name, module), v) => name == v.name && module == v.module}
      .values
      .map(x => (x, topLevelDefs.find(y => x.name == y.procedure.name && x.module == y.procedure.module).get))
      .map{case (x, y) => astBackifier.backify(x, y)}
      .toSeq
  }
  private def fromApiProcedure(p: FrontEndProcedure): Procedure = {
    val proc = new Procedure(
      isReporter = p.isReporter,
      name = p.name,
      nameToken = p.nameToken,
      argTokens = p.argTokens,
      procedureDeclaration = p.procedureDeclaration,
      module = p.module,
      baseDisplayName = if (p.displayName == "") None else Some(p.displayName)
    )
    proc.agentClassString = p.agentClassString
    proc.topLevel = p.topLevel
    proc.aliases = p.aliases
    proc.args = p.args
    proc.size = p.args.length
    proc
  }
}
