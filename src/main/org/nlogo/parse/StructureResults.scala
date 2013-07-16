// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, nvm },
  api.Token,
  nvm.ParserInterface.{ ProceduresMap, NoProcedures }

object StructureResults {
  val empty = StructureResults(program = api.Program.empty)
}
case class StructureResults(
  program: api.Program,
  procedures: ProceduresMap = NoProcedures,
  tokens: Map[nvm.Procedure, Iterable[Token]] = Map(),
  includes: Seq[Token] = Seq(),
  extensions: Seq[Token] = Seq())
