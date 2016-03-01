// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import FrontEndInterface._

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[String, Iterable[Token]] = Map(),
                        includes: Seq[Token] = Seq(),
                        extensions: Seq[Token] = Seq())

object StructureResults {
  val empty = StructureResults(Program.empty())
}
