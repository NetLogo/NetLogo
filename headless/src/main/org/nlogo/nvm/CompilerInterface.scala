// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api, api.{ Program, ExtensionManager }
import collection.immutable.ListMap

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

object CompilerInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  type ProceduresMap = ListMap[String, Procedure]
  val NoProcedures: ProceduresMap = ListMap()
}

trait CompilerInterface extends ParserInterface {
  import CompilerInterface.ProceduresMap
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager,
    flags: CompilerFlags = CompilerFlags()): CompilerResults
  def compileMoreCode(source: String, displayName: Option[String], program: Program,
    oldProcedures: ProceduresMap, extensionManager: ExtensionManager,
    flags: CompilerFlags = CompilerFlags()): CompilerResults
}

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = api.Version.useGenerator,
  useOptimizer: Boolean = api.Version.useOptimizer)

case class CompilerResults(procedures: Seq[Procedure], program: Program) {
  import collection.immutable.ListMap
  def proceduresMap =
    ListMap(procedures.map(proc => (proc.name, proc)): _*)
  def head = procedures.head
}
