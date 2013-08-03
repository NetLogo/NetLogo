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

import org.nlogo.{ api, nvm, parse0 },
  api.Token,
  nvm.ParserInterface.{ ProceduresMap, NoProcedures },
  Fail._

object StructureParser {

  /// main entry point.  handles gritty extensions stuff and includes stuff.

  def parseAll(
      tokenizer: api.TokenizerInterface,
      source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager): StructureResults = {
    if(!subprogram)
      extensionManager.startFullCompilation()
    val sources = Seq((source, ""))
    val oldResults = StructureResults(program, oldProcedures)
    def parseOne(source: String, filename: String, previousResults: StructureResults) = {
      val tokens =
        tokenizer.tokenize(source, filename)
          .filter(_.tpe != api.TokenType.Comment)
          .map(parse0.Namer0)
      new StructureParser(tokens, displayName, previousResults)
        .parse(subprogram)
    }
    val firstResults =
      sources.foldLeft(oldResults){
        case (results, (source, filename)) =>
          parseOne(source, filename, results)
      }
    val results =
      Iterator.iterate(firstResults){results =>
        assert(!subprogram)
        val path = extensionManager.resolvePath(results.includes.head.value.asInstanceOf[String])
        cAssert(path.endsWith(".nls"),
          "Included files must end with .nls",
          results.includes.head)
        val newResults =
          parseOne(api.FileIO.file2String(path), path, results)
        newResults.copy(includes = newResults.includes.filterNot(_ == results.includes.head))
      }.dropWhile(_.includes.nonEmpty).next
    if(!subprogram) {
      for(token <- results.extensions)
        extensionManager.importExtension(
          token.text.toLowerCase, new api.ErrorSource(token))
      extensionManager.finishFullCompilation()
    }
    results
  }

  val alwaysUsedNames =
    Parser.tokenMapper.allCommandNames.map(_ -> "primitive command") ++
    Parser.tokenMapper.allReporterNames.map(_ -> "primitive reporter")

}

/// for each source file. knits stages together. throws CompilerException

class StructureParser(
  tokens: Iterator[Token],
  displayName: Option[String],
  oldResults: StructureResults) {

  def parse(subprogram: Boolean): StructureResults =
    parse0.StructureCombinators.parse(tokens) match {
      case Right(declarations) =>
        parse0.StructureChecker.rejectDuplicateDeclarations(declarations)
        parse0.StructureChecker.rejectDuplicateNames(
          declarations, usedNames(oldResults.program, oldResults.procedures))
        StructureConverter.convert(declarations, displayName,
          if (subprogram)
            StructureResults.empty.copy(program = oldResults.program)
          else oldResults,
          subprogram)
      case Left((msg, token)) =>
        exception(msg, token)
    }

  def usedNames(program: api.Program, procedures: ProceduresMap): Map[String, String] =
    program.usedNames ++
    procedures.keys.map(_ -> "procedure") ++
    StructureParser.alwaysUsedNames

}
