// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.collection.immutable.ListMap
import FrontEndInterface._

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: ListMap[(String, Option[String]), Iterable[Token]] = ListMap(),
                        includes: Seq[Token] = Seq(),
                        includedSources: Seq[String] = Seq(),
                        extensions: Seq[Token] = Seq(),
                        imports: Seq[Import] = Seq(),
                        `export`: Option[Export] = None)

object StructureResults {
  val empty = StructureResults(Program.empty())
}
