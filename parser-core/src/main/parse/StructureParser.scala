// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

// For each source file, input is Tokens, output is a StructureResults -- which is mostly just a
// Program and some Procedures.

// Each source file is handled in three stages, each represented as a separate trait.
// 1. StructureCombinators parses input tokens according to a context-free grammar,
//    returning a Seq[Declaration].
// 2. StructureChecker checks the Seq[Declaration] for duplicates.
// 3. StructureConverter converts the Seq[Declaration] to a StructureResults.
// By splitting it this way, we get separation of concerns between the (clean) mechanics of parsing
// and the (messy) mechanics of building the data structures that the rest of the compiler and the
// engine will use.

// Note that when parsing starts, we don't necessarily have all our source files yet.  Some of them
// will be discovered as we parse, through __include declarations.  (Included files might themselves
// include further files.)

import
  org.nlogo.core,
    core.{ErrorSource, ExtensionManager, BreedIdentifierHandler, FrontEndInterface, Program, Token, StructureDeclarations, StructureResults},
      FrontEndInterface.ProceduresMap,
    core.Fail._

object StructureParser {

  /// main entry point.  handles gritty extensions stuff and includes stuff.

  def parseAll(
                tokenizer: core.TokenizerInterface,
                source: String, displayName: Option[String], program: Program, subprogram: Boolean,
                oldProcedures: ProceduresMap, extensionManager: ExtensionManager): StructureResults = {
    if (!subprogram)
      extensionManager.startFullCompilation()
    val sources = Seq((source, ""))
    val oldResults = StructureResults(program, oldProcedures)
    def parseOne(source: String, filename: String, previousResults: StructureResults): StructureResults = {
      val tokens =
        tokenizer.tokenizeString(source, filename)
          .filter(_.tpe != core.TokenType.Comment)
          .map(Namer0)
      new StructureParser(tokens, displayName, previousResults)
        .parse(subprogram)
    }
    val firstResults =
      sources.foldLeft(oldResults) {
        case (results, (source, filename)) =>
          parseOne(source, filename, results)
      }
    val results =
      Iterator.iterate(firstResults) { results =>
        assert(!subprogram)
        val suppliedPath = results.includes.head.value.asInstanceOf[String]
        cAssert(suppliedPath.endsWith(".nls"),
          "Included files must end with .nls",
          results.includes.head)
        IncludeFile(extensionManager, suppliedPath) match {
          case Some((path, fileContents)) =>
            parseOne(fileContents, path, results.copy(includes = results.includes.tail))
          case None =>
            exception(s"Could not find $suppliedPath", results.includes.head)
        }
      }.dropWhile(_.includes.nonEmpty).next
    if (!subprogram) {
      for (token <- results.extensions)
        extensionManager.importExtension(
          token.text.toLowerCase, new ErrorSource(token))
      extensionManager.finishFullCompilation()
    }
    results
  }

  val alwaysUsedNames =
    FrontEnd.tokenMapper.allCommandNames.map(_ -> "primitive command") ++
      FrontEnd.tokenMapper.allReporterNames.map(_ -> "primitive reporter")

  def usedNames(program: Program, procedures: ProceduresMap, declarations: Seq[StructureDeclarations.Declaration] = Seq()): Map[String, String] = {
    program.usedNames ++
      breedPrimitives(declarations) ++
      procedures.keys.map(_ -> "procedure") ++
      StructureParser.alwaysUsedNames
  }

  private def breedPrimitives(declarations: Seq[StructureDeclarations.Declaration]): Map[String, String] = {
    import BreedIdentifierHandler._
    import org.nlogo.core.StructureDeclarations.Breed

    declarations.flatMap {
      case breed: Breed =>
        val pairs = Seq(breedCommands _ -> "breed command", breedReporters _ -> "breed reporter", breedHomonymProcedures _ -> "breed")
        pairs flatMap { case (f, label) => f(breed).map(_ -> label) }
      case _ => Seq()
    }.toMap
  }
}
/// for each source file. knits stages together. throws CompilerException

class StructureParser(
  tokens: Iterator[Token],
  displayName: Option[String],
  oldResults: StructureResults) {

  def parse(subprogram: Boolean): StructureResults =
    StructureCombinators.parse(tokens) match {
      case Right(declarations) =>
        StructureChecker.rejectDuplicateDeclarations(declarations)
        StructureChecker.rejectDuplicateNames(declarations,
          StructureParser.usedNames(
            oldResults.program, oldResults.procedures, declarations))
        StructureConverter.convert(declarations, displayName,
          if (subprogram)
            StructureResults(program = oldResults.program)
          else oldResults,
          subprogram)
      case Left((msg, token)) =>
        exception(msg, token)
    }

}
