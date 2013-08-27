// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api, api.{ Program, ExtensionManager }

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {
  import FrontEndInterface.ProceduresMap
  def frontEnd: FrontEndInterface
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
