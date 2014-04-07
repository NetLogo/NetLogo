// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ core, api },
  FrontEndInterface.{ ProceduresMap, NoProcedures }

object StructureResults {
  val empty = StructureResults(program = api.Program.empty)
}
case class StructureResults(
  program: api.Program,
  procedures: ProceduresMap = NoProcedures,
  tokens: Map[Procedure, Iterable[core.Token]] = Map(),
  includes: Seq[core.Token] = Seq(),
  extensions: Seq[core.Token] = Seq())
