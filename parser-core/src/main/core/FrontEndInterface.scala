// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.nlogo.core.FrontEndInterface.{NoProcedures, ProceduresMap}
import java.text.CharacterIterator

object FrontEndInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  import scala.collection.immutable.ListMap
  // The first string in the key is the procedure name. The second string is the name of the module it's supposed to be
  // visible in, if any. For procedures visible to the model, this will be None. - Kritphong M November 2025
  type ProceduresMap = ListMap[(String, Option[String]), FrontEndProcedure]
  val NoProcedures: ProceduresMap = ListMap()
  type FrontEndResults = (Seq[ProcedureDefinition], StructureResults)

  def hasIncludes(source: String): Boolean = {
    val includesRegEx = """(?m)^\s*__includes""".r
    includesRegEx.findFirstMatchIn(source) match {
      case Some(_) => true
      case None    => false
    }
  }

  def hasImport(source: String): Boolean = {
    val importsRegEx = """(?m)^\s*import""".r
    importsRegEx.findFirstMatchIn(source) match {
      case Some(_) => true
      case None    => false
    }
  }

}

case class ProcedureSyntax(declarationKeyword: Token, identifier: Token, endKeyword: Token)

case class CompilationOperand(
  sources: Map[String, String],
  extensionManager: ExtensionManager,
  libraryManager: LibraryManager,
  compilationEnvironment: CompilationEnvironment,
  containingProgram: Program = Program.empty(),
  oldProcedures: ProceduresMap = NoProcedures,
  subprogram: Boolean = true,
  // displayName is only used by reporters in slider widgets.
  // I would like to eliminate it, but not right now.
  displayName: Option[String] = None,
  shouldAutoInstallLibs: Boolean = false)

trait FrontEndInterface {
  def frontEnd(
        source: String,
        displayName: Option[String] = None,
        program: Program = Program.empty(),
        subprogram: Boolean = true,
        oldProcedures: ProceduresMap = NoProcedures,
        extensionManager: ExtensionManager = new DummyExtensionManager,
        libraryManager: LibraryManager = new DummyLibraryManager,
        compilationEnvironment: CompilationEnvironment = new DummyCompilationEnvironment,
        shouldAutoInstallLibs: Boolean = false)
      : FrontEndInterface.FrontEndResults = {
    frontEnd(
      CompilationOperand( Map("" -> source), extensionManager, libraryManager, compilationEnvironment
                        , program, oldProcedures, subprogram, displayName, shouldAutoInstallLibs)
    )
  }

  def frontEnd(compilationOperand: CompilationOperand): FrontEndInterface.FrontEndResults

  // matches procedure definitions to procedure syntax objects
  // does not error on bad parse
  def findProcedurePositions(source: String, dialect: Option[Dialect]): Map[String, ProcedureSyntax]

  // lists the strings contained by the __includes list
  def findIncludes(source: String): Seq[String]

  // lists the names of imported libraries
  def findImports(source: String): Seq[(Option[String], String)]

  // these do enough tokenization to be used by the frontEnd.
  // It's up to to the caller to decide whether they want a Seq or an Iterator
  def tokenizeForColorization(source: String, dialect: Dialect, extensionManager: ExtensionManager): Seq[Token]
  def tokenizeForColorizationIterator(source: String, dialect: Dialect, extensionManager: ExtensionManager): Iterator[Token]

  def tokenizeWithWhitespace(source: String, dialect: Dialect, extensionManager: ExtensionManager): Iterator[Token]

  def tokenizeWithWhitespaceConsolidated(iter: CharacterIterator, filename: String): Iterator[Token]

  def findExtensions(source: String): Seq[String]
}
