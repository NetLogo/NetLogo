// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.nlogo.core.FrontEndInterface.{NoProcedures, ProceduresMap}

object FrontEndInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  import scala.collection.immutable.ListMap
  type ProceduresMap = ListMap[String, FrontEndProcedure]
  val NoProcedures: ProceduresMap = ListMap()
  type FrontEndResults = (Seq[ProcedureDefinition], StructureResults)

}

case class ProcedureSyntax(declarationKeyword: Token, identifier: Token, endKeyword: Token)

case class CompilationOperand(
  sources: Map[String, String],
  extensionManager: ExtensionManager,
  compilationEnvironment: CompilationEnvironment,
  containingProgram: Program = Program.empty,
  oldProcedures: ProceduresMap = NoProcedures,
  subprogram: Boolean = true,
  // displayName is only used by reporters in slider widgets.
  // I would like to eliminate it, but not right now.
  displayName: Option[String] = None)

trait FrontEndInterface {
  def frontEnd(
        source: String,
        displayName: Option[String] = None,
        program: Program = Program.empty(),
        subprogram: Boolean = true,
        oldProcedures: ProceduresMap = NoProcedures,
        extensionManager: ExtensionManager = new DummyExtensionManager,
        compilationEnvironment: CompilationEnvironment = new DummyCompilationEnvironment)
      : FrontEndInterface.FrontEndResults = {
    frontEnd(CompilationOperand(Map("" -> source), extensionManager, compilationEnvironment, program, oldProcedures, subprogram, displayName))
  }

  def frontEnd(compilationOperand: CompilationOperand): FrontEndInterface.FrontEndResults

  // matches procedure definitions to procedure syntax objects
  // does not error on bad parse
  def findProcedurePositions(source: String, dialect: Option[Dialect]): Map[String, ProcedureSyntax]

  // lists the strings contained by the __includes list
  def findIncludes(source: String): Seq[String]

  // does enough tokenization to be used by the frontEnd
  def tokenizeForColorization(source: String, dialect: Dialect, extensionManager: ExtensionManager): Seq[Token]
}

