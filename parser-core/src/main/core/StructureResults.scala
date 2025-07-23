// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.nlogo.core.StructureDeclarations.ExtensionDeclaration
import FrontEndInterface._

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[String, Iterable[Token]] = Map(),
                        includes: Seq[Token] = Seq(),
                        includedSources: Seq[String] = Seq(),
                        extensions: Seq[Token] = Seq(),
                        configurableExtensions: Seq[ExtensionDeclaration] = Seq())

object StructureResults {
  val empty = StructureResults(Program.empty())
}
