// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import FrontEndInterface._

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[Tuple2[String, Option[String]], Iterable[Token]] = Map(),
                        includes: Seq[Token] = Seq(),
                        includedSources: Seq[String] = Seq(),
                        extensions: Seq[Token] = Seq(),
                        imports: Seq[Import] = Seq(),
                        defineLibrary: Option[DefineLibrary] = None)

object StructureResults {
  val empty = StructureResults(Program.empty())
}
