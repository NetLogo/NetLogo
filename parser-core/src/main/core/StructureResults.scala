// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import FrontEndInterface._

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[(String, Option[String]), Iterable[Token]] = Map(),
                        includes: Seq[Token] = Seq(),
                        includedSources: Seq[String] = Seq(),
                        extensions: Seq[Token] = Seq(),
                        imports: Seq[Import] = Seq(),
                        _export: Option[Export] = None)

object StructureResults {
  val empty = StructureResults(Program.empty())
}
