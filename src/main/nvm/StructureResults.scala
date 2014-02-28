// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api,
  FrontEndInterface.{ ProceduresMap, NoProcedures }

object StructureResults {
  val empty = StructureResults(program = api.Program.empty)
}
case class StructureResults(
  program: api.Program,
  procedures: ProceduresMap = NoProcedures,
  tokens: Map[Procedure, Iterable[api.Token]] = Map(),
  includes: Seq[api.Token] = Seq(),
  extensions: Seq[api.Token] = Seq())
