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
import scala.collection.immutable.ListMap
import org.nlogo.core.{ Breed, CompilationEnvironment, CompilationOperand, CompilerException, ErrorSource, I18N,
                        ProcedureSyntax, Program, StructureResults, Token, TokenizerInterface, TokenType }
import org.nlogo.core.Fail._
import org.nlogo.core.FrontEndInterface.ProceduresMap
import org.nlogo.core.LibraryStatus.CanInstall
import org.nlogo.util.PathUtils

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
          val (maybeDuplicateToken, _) = firstResults.libraries.map(_.token).foldLeft((None: Option[Token], Set(): Set[Token])) {
            case ((None, previousTokens), x) => (if (previousTokens.contains(x)) Some(x) else None, previousTokens + x)

            // No need to update previousTokens now that we've found something
            case ((token @ Some(_), previousTokens), _) => (token, previousTokens)
          }

          maybeDuplicateToken.foreach(exception(I18N.errors.get("compiler.StructureParser.libraryMultipleImports"), _))

          var processedLibraries: Set[String] = Set()

          Iterator.iterate(firstResults) { results =>
            var newResults: StructureResults = results

            // Handle libraries
            if (newResults.libraries.nonEmpty) {
              val filename = newResults.libraries.head.name.toLowerCase + ".nls"
              val suppliedPath = resolveIncludePath(filename)

              val previousResults = newResults
              val currentLibrary = results.libraries.head

              newResults = includeFile(compilationEnvironment, suppliedPath) match {

                case Some((path, fileContents)) =>
                  parseOne(tokenizer, structureParser, fileContents, path,
                    newResults.copy(libraries = newResults.libraries.tail,
                      includedSources = newResults.includedSources :+ suppliedPath))
                case None =>
                  exception(I18N.errors.getN("compiler.StructureParser.libraryNotFound", suppliedPath), currentLibrary.token)
              }

              if (processedLibraries.contains(currentLibrary.name)) {
                exception(I18N.errors.getN("compiler.StructureParser.libraryImportLoop"), currentLibrary.token)
              } else {
                processedLibraries += currentLibrary.name
              }

              val prefix = currentLibrary.alias.getOrElse(currentLibrary.name) + ":"

              newResults = newResults.copy(
                program = prefixProgramChanges(previousResults.program, newResults.program, prefix),
                procedures = prefixChangedProcedures(previousResults.procedures, newResults.procedures, prefix),
                procedureTokens = prefixChangedProcedureTokens(previousResults.procedureTokens, newResults.procedureTokens, prefix))
            }

            // Handle includes
            if (newResults.includes.nonEmpty) {
              val suppliedPath = resolveIncludePath(newResults.includes.head.value.asInstanceOf[String])
              cAssert(suppliedPath.endsWith(".nls"), IncludeFilesEndInNLS, newResults.includes.head)
              newResults = includeFile(compilationEnvironment, suppliedPath) match {
                case Some((path, fileContents)) =>
                  parseOne(tokenizer, structureParser, fileContents, path,
                    newResults.copy(includes = newResults.includes.tail,
                      includedSources = newResults.includedSources :+ suppliedPath))
                case None =>
                  exception(I18N.errors.getN("compiler.StructureParser.includeNotFound", suppliedPath), newResults.includes.head)
              }
            }

            newResults
          }.dropWhile(x => x.includes.nonEmpty || x.libraries.nonEmpty).next()
        }
      }
  }

  private def prefixProgramChanges(oldProgram: Program, newProgram: Program, prefix: String): Program = {
    val oldUserGlobalsCount = oldProgram.userGlobals.size
    val oldTurtleVarsCount = oldProgram.turtleVars.size
    val oldPatchVarsCount = oldProgram.patchVars.size

    val (oldUserGlobals, changedUserGlobals) = newProgram.userGlobals.splitAt(oldUserGlobalsCount)
    val (oldTurtleVars, changedTurtleVars) = newProgram.turtleVars.splitAt(oldTurtleVarsCount)
    val (oldPatchVars, changedPatchVars) = newProgram.patchVars.splitAt(oldPatchVarsCount)

    newProgram.copy(
      userGlobals = oldUserGlobals ++ changedUserGlobals.map(prefix + _),
      turtleVars = oldTurtleVars ++ changedTurtleVars.map {case (k, v) => prefix + k -> v},
      patchVars = oldPatchVars ++ changedPatchVars.map {case (k, v) => prefix + k -> v},
      breeds = prefixChangedBreeds(oldProgram.breeds, newProgram.breeds, prefix),
      linkBreeds = prefixChangedBreeds(oldProgram.linkBreeds, newProgram.linkBreeds, prefix))
  }

  private def prefixChangedBreeds(oldBreeds: ListMap[String, Breed], newBreeds: ListMap[String, Breed], prefix: String): ListMap[String, Breed] = {
    val oldBreeds_ = oldBreeds.zip(newBreeds.values).map {case ((k, oldBreed), newBreed) =>
      k -> prefixChangedBreedOwns(oldBreed, newBreed, prefix)
    }.to(ListMap)
    val newBreeds_ = newBreeds.drop(oldBreeds.size).map {case (k, v) =>
      prefix + k -> v.copy(
        name = prefix + v.name,
        singular = prefix + v.singular,
        owns = v.owns.map(prefix + _))
    }.to(ListMap)

    oldBreeds_ ++ newBreeds_
  }

  private def prefixChangedBreedOwns(oldBreed: Breed, newBreed: Breed, prefix: String): Breed = {
    val changedOwns = newBreed.owns.diff(oldBreed.owns)
    newBreed.copy(owns = oldBreed.owns ++ changedOwns.map(prefix + _))
  }

  private def prefixChangedProcedures(oldProcedures: ProceduresMap, newProcedures: ProceduresMap, prefix: String): ProceduresMap = {
    val changedProcedures = newProcedures.drop(oldProcedures.size).map{case ((name, filename), proc) =>
      val decl = proc.procedureDeclaration
      val newName = prefix.toUpperCase + proc.name
      val newToken: Token = decl.name.token.copy(text = prefix + proc.name, value = prefix.toUpperCase + decl.name.token.value)(decl.name.token.sourceLocation)
      val newTokens = decl.tokens.updated(1, newToken) // The token at index 1 is the name of the procedure
      val newDecl = decl.copy(name = decl.name.copy(name = newName, token = newToken), tokens = newTokens)

      (prefix.toUpperCase + name, filename) -> new RawProcedure(newDecl, None)}

    oldProcedures ++ changedProcedures
  }

  private def prefixChangedProcedureTokens(oldProcedureTokens: Map[String, Iterable[Token]], newProcedureTokens: Map[String, Iterable[Token]], prefix: String): Map[String, Iterable[Token]] = {
    val changedProcedureTokens = newProcedureTokens.drop(oldProcedureTokens.size).map{case (name, proc) =>
      prefix.toUpperCase + name -> proc
    }

    oldProcedureTokens ++ changedProcedureTokens
  }

  // TODO: extend to work with modules
  private def parsingWithExtensions(compilationData: CompilationOperand)
                                   (results: => StructureResults): StructureResults = {
    if (compilationData.subprogram)
      results
    else {
      compilationData.extensionManager.startFullCompilation()

      val r = results

      for (token <- r.extensions) {
        val text = token.text.toLowerCase
        if (compilationData.shouldAutoInstallLibs) {
          val lm = compilationData.libraryManager
          lm.lookupExtension(text, "").filter(_.status == CanInstall).foreach(lm.installExtension)
        }
        compilationData.extensionManager.importExtension(text, new ErrorSource(token))
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
      structureParser.parse(tokens, oldResults, filename)
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
        .addSymbols(procedures.keys.map(_._1), SymbolType.ProcedureSymbol)

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
            case s: String => PathUtils.standardize(resolveIncludePath(s))
          }.toSeq
    }
  }

  @throws(classOf[CompilerException])
  def findLibraries(tokens: Iterator[Token]): Seq[String] = {
    val libraryPositionedTokens =
      tokens.dropWhile(! _.text.equalsIgnoreCase("library"))
    val result =
      if (libraryPositionedTokens.isEmpty)
        Seq()
      else {
        libraryPositionedTokens.next()
        val libraryWithoutComments = libraryPositionedTokens.filter(_.tpe != TokenType.Comment)
        if (libraryWithoutComments.next().tpe != TokenType.OpenBracket)
          exception("Did not find expected open bracket for library declaration", tokens.next())
        else
          libraryWithoutComments
            .takeWhile((x) => x.tpe != TokenType.OpenBracket && x.tpe != TokenType.CloseBracket)
            .filter(_.tpe == TokenType.Ident)
            .map(_.value.toString)
            .toSeq
      }
    if (result.isEmpty) {
      result
    } else {
      result ++ findLibraries(tokens)
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
}

/// for each source file. knits stages together. throws CompilerException

class StructureParser(
  displayName: Option[String],
  subprogram: Boolean) {

  def parse(tokens: Iterator[Token], oldResults: StructureResults, filename: String): StructureResults =
    StructureCombinators.parse(tokens, filename) match {
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
