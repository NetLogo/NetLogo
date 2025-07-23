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

import java.util.Locale
import scala.collection.mutable.ListBuffer

import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, CompilerException, ErrorSource, I18N, ProcedureSyntax, Program, StructureResults, Token, TokenizerInterface, TokenType }
import org.nlogo.core.Fail._
import org.nlogo.core.FrontEndInterface.ProceduresMap
import org.nlogo.core.LibraryStatus.CanInstall

object StructureParser {
  val IncludeFilesEndInNLS = "Included files must end with .nls"

  /// main entry point.  handles gritty extensions stuff and includes stuff.
  def parseSources(tokenizer: TokenizerInterface, compilationData: CompilationOperand,
    includeFile: (CompilationEnvironment, String) => Option[(String, String)] = IncludeFile.apply): StructureResults = {
      import compilationData.{ compilationEnvironment, displayName, oldProcedures, subprogram, sources, containingProgram => program }
      parsingWithExtensions(compilationData) {
        val structureParser = new StructureParser(displayName, subprogram)
        val firstResults =
          sources.foldLeft(StructureResults(program, oldProcedures)) {
            case (results, (filename, source)) =>
              parseOne(tokenizer, structureParser, source, filename, results)
          }
        if (subprogram)
          firstResults
        else {
          Iterator.iterate(firstResults) { results =>
            val suppliedPath = resolveIncludePath(results.includes.head.value.asInstanceOf[String])
            cAssert(suppliedPath.endsWith(".nls"), IncludeFilesEndInNLS, results.includes.head)
            includeFile(compilationEnvironment, suppliedPath) match {
              case Some((path, fileContents)) =>
                parseOne(tokenizer, structureParser, fileContents, path,
                  results.copy(includes = results.includes.tail,
                    includedSources = results.includedSources :+ suppliedPath))
              case None =>
                exception(I18N.errors.getN("compiler.StructureParser.includeNotFound", suppliedPath), results.includes.head)
            }
          }.dropWhile(_.includes.nonEmpty).next()
        }
      }
  }

  private def parsingWithExtensions(compilationData: CompilationOperand)
                                   (results: => StructureResults): StructureResults = {
    if (compilationData.subprogram)
      results
    else {
      compilationData.extensionManager.startFullCompilation()

      val r = results
      val allExtensions =
        r.extensions.map(                    // Old extensions
          e => (
            e.text.toLowerCase,
            None,
            new ErrorSource(e)
        )) ++
        r.configurableExtensions.map(        // Configurable extensions
          e => (
            e.name.name.toLowerCase,
            e.url.map(_.name),
            new ErrorSource(e.name.token)
          )
        )
      

      for ((name, url, errorSource) <- allExtensions) {
        if (compilationData.shouldAutoInstallLibs) {
          val lm = compilationData.libraryManager
          lm.lookupExtension(name, "").filter(_.status == CanInstall).foreach(lm.installExtension)
        }
        compilationData.extensionManager.importExtension(name, url, errorSource) // New importExtension method
      }

      compilationData.extensionManager.finishFullCompilation()

      r
    }
  }

  private def parseOne(tokenizer: TokenizerInterface, structureParser: StructureParser, source: String, filename: String, oldResults: StructureResults): StructureResults = {
      val tokens =
        tokenizer.tokenizeString(source, filename)
          .filter(_.tpe != TokenType.Comment)
          .map(Namer0)
      structureParser.parse(tokens, oldResults)
    }

  private[parse] def usedNames(program: Program, procedures: ProceduresMap): SymbolTable = {
    val symTable =
      SymbolTable.empty
        .addSymbols(program.dialect.tokenMapper.allCommandNames, SymbolType.PrimitiveCommand)
        .addSymbols(program.dialect.tokenMapper.allReporterNames, SymbolType.PrimitiveReporter)
        .addSymbols(program.globals, SymbolType.GlobalVariable)
        .addSymbols(program.turtlesOwn, SymbolType.TurtleVariable)
        .addSymbols(program.patchesOwn, SymbolType.PatchVariable)
        .addSymbols(program.linksOwn.filterNot(program.turtlesOwn.contains), SymbolType.LinkVariable)
        .addSymbols(program.breeds.values.map(_.singular), SymbolType.TurtleBreedSingular)
        .addSymbols(program.breeds.keys, SymbolType.TurtleBreed)
        .addSymbols(program.linkBreeds.values.map(_.singular), SymbolType.LinkBreedSingular)
        .addSymbols(program.linkBreeds.keys, SymbolType.LinkBreed)
        .addSymbols(procedures.keys, SymbolType.ProcedureSymbol)

    program.breeds.values.foldLeft(symTable) {
      case (table, breed) if breed.isLinkBreed =>
        table.addSymbols(breed.owns, SymbolType.LinkBreedVariable(breed.name))
      case (table, breed) =>
        table.addSymbols(breed.owns, SymbolType.BreedVariable(breed.name))
    }
  }

  def findProcedurePositions(tokens: Seq[Token]): Map[String, ProcedureSyntax] = {
    import scala.annotation.tailrec
    def procedureSyntax(tokens: Seq[Token]): Option[(String, ProcedureSyntax)] = {
      val ident = tokens(1)
      if (ident.tpe == TokenType.Ident)
        Some((ident.text, ProcedureSyntax(tokens.head, ident, tokens.last)))
      else
        None
    }

    @tailrec
    def splitOnProcedureStarts(tokens:       Seq[Token],
                              existingProcs: Seq[Seq[Token]]): Seq[Seq[Token]] = {
      if (tokens.isEmpty || tokens.head.tpe == TokenType.Eof)
        existingProcs
      else {
        val headValue = tokens.head.value
        if (headValue == "TO" || headValue == "TO-REPORT") {
          val sizeToEnd = tokens.takeWhile(t => t.value != "END").size
          val (procedureTokens, remainingTokens) = tokens.splitAt(sizeToEnd + 1)
          splitOnProcedureStarts(remainingTokens, existingProcs :+ procedureTokens)
        } else {
          splitOnProcedureStarts(
            tokens.dropWhile(t => ! (t.value == "TO" || t.value == "TO-REPORT")),
            existingProcs)
        }
      }
    }

    splitOnProcedureStarts(tokens, Seq()).flatMap(procedureSyntax).toMap
  }
  @throws(classOf[CompilerException])
  def findIncludes(tokens: Iterator[Token]): Seq[String] = {
    val includesPositionedTokens =
      tokens.dropWhile(! _.text.equalsIgnoreCase("__includes"))
    if (includesPositionedTokens.isEmpty)
      Seq()
    else {
      includesPositionedTokens.next()
      val includesWithoutComments = includesPositionedTokens.filter(_.tpe != TokenType.Comment)
      if (includesWithoutComments.next().tpe != TokenType.OpenBracket)
        exception("Did not find expected open bracket for __includes declaration", tokens.next())
      else
        includesWithoutComments
          .takeWhile(_.tpe != TokenType.CloseBracket)
          .filter(_.tpe == TokenType.Literal)
          .map(_.value)
          .collect {
            case s: String => resolveIncludePath(s)
          }.toSeq
    }
  }

  def resolveIncludePath(path: String) = {
    val name = System.getProperty("os.name")

    if (name == null || name.startsWith("Windows"))
      path
    else
      path.replaceFirst("^~", System.getProperty("user.home"))
  }

  def findExtensions(tokens: Iterator[Token]): Seq[String] = {
    val openBracket = tokens.dropWhile(!_.text.equalsIgnoreCase("extensions")).drop(1)
                            .dropWhile(_.tpe == TokenType.Comment)

    if (openBracket.isEmpty || openBracket.next().tpe != TokenType.OpenBracket) {
      Seq()
    } else {
      openBracket.takeWhile(_.tpe != TokenType.CloseBracket).filter(_.tpe == TokenType.Ident)
                 .map(_.value.toString).toSeq
    }
  }

  def findConfigurableExtensions(tokens: Iterator[Token]): Seq[(String, Option[String])] = {
    // Look for the keyword "EXTENSION"
    // syntax: extension [name [url <url>]?]
    val extensions = ListBuffer[(String, Option[String])]()
    while (tokens.hasNext) {
      val token = tokens.next() // Keep moving until we find "EXTENSION"
      if (token.tpe == TokenType.Keyword && token.value == "EXTENSION") {
        // Open bracket
        if (!tokens.hasNext || tokens.next().tpe != TokenType.OpenBracket) {
          exception("Expected open bracket after 'EXTENSION' keyword", token)
        }

        // Identifier for the extension name
        val nameToken = tokens.next()
        if (nameToken.tpe != TokenType.Ident) {
          exception("Expected identifier after 'EXTENSION' keyword", nameToken)
        }

        // Optional URL
        var url: Option[String] = None
        if (tokens.hasNext) { 
          val nextToken = tokens.next() // [url <url>]? is optional + ]
          if (nextToken.tpe == TokenType.OpenBracket) {
            // We have a URL specification
            if (!tokens.hasNext || !tokens.next().value.toString.equalsIgnoreCase("url")) {
              exception("Expected 'url' keyword after open bracket", nextToken)
            }
            val urlToken = tokens.next()
            if (urlToken.tpe != TokenType.Literal) {
              exception("Expected URL literal", urlToken)
            }
            if (!tokens.hasNext || tokens.next().tpe != TokenType.CloseBracket) {
              exception("Expected close bracket after URL", urlToken)
            }
            url = Some(urlToken.value.toString)
            
            // Now consume the final close bracket
            if (!tokens.hasNext || tokens.next().tpe != TokenType.CloseBracket) {
              exception("Expected close bracket after extension declaration", urlToken)
            }
          } else if (nextToken.tpe == TokenType.CloseBracket) {
            // No URL, just the closing bracket - this is fine
          } else {
            exception("Expected close bracket or URL specification", nextToken)
          }
        } else {
          exception("Expected close bracket after extension name", nameToken)
        }

        // Add the extension to the list
        extensions.addOne((nameToken.value.toString, url))
      }
    }
    extensions.toSeq
  }
}

/// for each source file. knits stages together. throws CompilerException

class StructureParser(
  displayName: Option[String],
  subprogram: Boolean) {

  def parse(tokens: Iterator[Token], oldResults: StructureResults): StructureResults =
    StructureCombinators.parse(tokens) match {
      case Right(declarations) =>
        StructureChecker.rejectMisplacedConstants(declarations)
        StructureChecker.rejectDuplicateDeclarations(declarations)
        StructureChecker.rejectDuplicateNames(declarations,
          StructureParser.usedNames(
            oldResults.program, oldResults.procedures))
        StructureChecker.rejectMissingReport(declarations)
        StructureConverter.convert(declarations, displayName,
          if (subprogram)
            StructureResults(program = oldResults.program)
          else oldResults,
          subprogram)
      case Left((msg, token)) =>
        if (token.tpe == TokenType.Keyword) {
          exception(s"""Keyword ${token.text.toUpperCase(Locale.ENGLISH)} cannot be used in this context.""", token)
        } else {
          exception(msg, token)
        }
    }

}
