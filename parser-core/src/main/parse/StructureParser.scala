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

import scala.collection.immutable.ListMap
import scala.collection.mutable
import java.util.Locale
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, CompilerException, ErrorSource, I18N, Import,
                        ProcedureSyntax, Program, StructureDeclarations, StructureResults, Token, TokenizerInterface,
                        TokenType }
import org.nlogo.core.Fail._
import org.nlogo.core.FrontEndInterface.{ ProceduresMap, ProcedureTokensMap }
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
              parseOne(tokenizer, structureParser, source, filename, None, results)
          }

        if (subprogram)
          firstResults
        else {
          // Check for duplicate import statements. We're doing this check instead of relying on the duplicate symbol
          // check so that we can provide a more specific error message.
          val (maybeDuplicateImport, _) = firstResults.imports.foldLeft((None: Option[Import], Set())) {
            case ((None, previousNames), x) => {
              val maybeDuplicate = if (previousNames.contains(x.pathComponents)) Some(x) else None
              (maybeDuplicate, previousNames + x.pathComponents)
            }

            // No need to update previousNames now that we've found something
            case ((token @ Some(_), previousNames), _) => (token, previousNames)
          }

          maybeDuplicateImport.foreach(x => exception(I18N.errors.get("compiler.StructureParser.importMultipleImports"), x.token))

          val moduleCache: mutable.Map[String, (ProceduresMap, ProcedureTokensMap)] = mutable.Map()

          Iterator.iterate(firstResults) { results =>
            var newResults: StructureResults = results

            // Handle imports
            if (newResults.imports.nonEmpty) {
              val currentImport = results.imports.head
              val separator = System.getProperty("file.separator")
              val newImports: mutable.Set[Import] = mutable.Set()

              val suppliedPaths =
                compilationEnvironment.resolveModulePath(currentImport.filename, currentImport.pathComponents)

              if (suppliedPaths.isEmpty) {
                exception(I18N.errors.getN("compiler.StructureParser.importNotFound", currentImport.pathComponents.mkString(":")), currentImport.token)
              }

              if (currentImport.importedIdentifiers.nonEmpty && suppliedPaths.length > 1) {
                exception(I18N.errors.getN("compiler.StructureParser.importSelectiveFromNonModule"), currentImport.token)
              }

              for (currentPath <- suppliedPaths) {
                val prefix =
                  currentImport.pathAlias match {
                    case Some(x) => s"$x:"
                    case None =>
                      if (currentImport.importedIdentifiers.nonEmpty) {
                        ""
                      } else if (suppliedPaths.length == 1) {
                        s"${currentImport.pathAlias.getOrElse(currentImport.pathComponents.last).toUpperCase}:"
                      } else {
                        val basePath = compilationEnvironment.resolvePath(currentImport.filename.getOrElse(""))
                        val relativePath = currentPath.drop(basePath.length + 1) // Strip common prefix plus a separator
                        val path = raw"(?i)\.nls$$".r.replaceFirstIn(relativePath.replace(separator, ":").toUpperCase, "")
                        s"$path:"
                      }
                  }

                if (!moduleCache.contains(currentPath)) {
                  newResults = includeFile(compilationEnvironment, currentPath) match {
                    case Some((path, fileContents)) => {
                      parseOne(tokenizer, structureParser, fileContents, path, Some(path),
                        newResults.copy(
                          imports = results.imports.tail,
                          includedSources = newResults.includedSources :+ currentPath))
                    }
                    case None =>
                      exception(I18N.errors.getN("compiler.StructureParser.importNotFound", currentPath), currentImport.token)
                  }

                  newImports ++= newResults.imports.toSet -- results.imports.toSet

                  val exportedNames =
                    newResults.`export`.map(_.exportedNames.toSet).getOrElse(newResults.procedures.keys.map(_._1).toSet)

                  val importedNames =
                    if (currentImport.importedIdentifiers.nonEmpty) {
                      currentImport.importedIdentifiers.keys.toSet
                    } else {
                      exportedNames
                    }

                  if ((importedNames -- exportedNames).nonEmpty) {
                    exception(I18N.errors.getN("compiler.StructureParser.importSelectiveNotExported"), currentImport.token)
                  }

                  def extractExported[V](
                    newMap: ListMap[(String, Option[String]), V],
                    oldMapKeys: IterableOnce[(String, Option[String])]): ListMap[(String, Option[String]), V] = {

                    newMap.removedAll(oldMapKeys).filter{case ((name, moduleFilename), _) =>
                      moduleFilename == Some(currentPath) && exportedNames.contains(name)
                    }
                  }

                  val exportedProcedures = extractExported(newResults.procedures, results.procedures.keys)
                  val exportedProcedureTokens = extractExported(newResults.procedureTokens, results.procedureTokens.keys)

                  moduleCache += (currentPath -> (exportedProcedures, exportedProcedureTokens))

                  val renameMap =
                    if (currentImport.importedIdentifiers.nonEmpty) {
                      currentImport.importedIdentifiers
                    } else {
                      Map.from(importedNames.map(x => (x, prefix.toUpperCase + x)))
                    }

                  val newProcedures = addProcedureAliases(
                    results.procedures,
                    newResults.procedures,
                    renameMap,
                    currentImport.filename
                  )
                  val newProcedureTokens = addProcedureTokenAliases(
                    results.procedureTokens,
                    newResults.procedureTokens,
                    renameMap,
                    currentImport.filename,
                    currentImport.filename
                  )

                  newResults = newResults.copy(
                    imports = newResults.imports ++ newImports.toSeq,
                    procedures = newProcedures,
                    procedureTokens = newProcedureTokens
                  )
                } else {
                  val (procedures, procedureTokens) = moduleCache(currentPath)

                  val procedureAliases = procedures.map {
                    case ((name, _), proc) =>
                      val key = (prefix.toUpperCase + name, currentImport.filename)
                      proc.aliases = proc.aliases :+ key
                      key -> proc
                  }

                  val procedureTokenAliases = procedureTokens.map {
                    case ((name, _), proc) => (prefix.toUpperCase + name, currentImport.filename) -> proc
                  }

                  newResults = newResults.copy(
                    imports = newResults.imports.tail,
                    procedures = newResults.procedures ++ procedureAliases,
                    procedureTokens = newResults.procedureTokens ++ procedureTokenAliases
                  )
                }
              }
            }

            // Handle includes
            if (newResults.includes.nonEmpty) {
              val suppliedPath = resolveIncludePath(newResults.includes.head.value.asInstanceOf[String])
              cAssert(suppliedPath.endsWith(".nls"), IncludeFilesEndInNLS, newResults.includes.head)
              newResults = includeFile(compilationEnvironment, suppliedPath) match {
                case Some((path, fileContents)) =>
                  parseOne(tokenizer, structureParser, fileContents, path, None,
                    newResults.copy(includes = newResults.includes.tail,
                      includedSources = newResults.includedSources :+ suppliedPath))
                case None =>
                  exception(I18N.errors.getN("compiler.StructureParser.includeNotFound", suppliedPath), newResults.includes.head)
              }
            }

            newResults
          }.dropWhile(x => x.includes.nonEmpty || x.imports.nonEmpty).next()
        }
      }
  }

  private def addProcedureAliases(
    oldProcedures: ProceduresMap,
    newProcedures: ProceduresMap,
    renameMap: Map[String, String],
    module: Option[String]
  ): ProceduresMap = {

    val changedProcedures = newProcedures.removedAll(oldProcedures.keys)
    val exportedProcedures = changedProcedures.filter {
      case ((name, _), _) => renameMap.contains(name)
    }

    val aliases = exportedProcedures.map{
      case ((name, _), proc) =>
        val key = (renameMap(name), module)
        proc.aliases = proc.aliases :+ key
        key -> proc
    }

    val oldProcedureKeys = oldProcedures.keys.toSet

    aliases.keys.find(x => oldProcedureKeys.contains(x)) match {
      case Some(x) => {
        val message = I18N.errors.getN("compiler.StructureParser.importConflict", "procedure", x._1)
        exception(message, oldProcedures(x).nameToken)
      }
      case None => ()
    }

    newProcedures ++ aliases
  }

  private def addProcedureTokenAliases(
    oldProcedureTokens: ListMap[(String, Option[String]), Iterable[Token]],
    newProcedureTokens: ListMap[(String, Option[String]), Iterable[Token]],
    renameMap: Map[String, String],
    module: Option[String],
    filename: Option[String]
  ): ListMap[(String, Option[String]), Iterable[Token]] = {

    val changedProcedureTokens = newProcedureTokens.removedAll(oldProcedureTokens.keys)
    val exportedProcedureTokens = changedProcedureTokens.filter {
      case ((name, _), _) => renameMap.contains(name)
    }

    // addProcedureAliases() already checks for name conflicts, so no need to check again here.
    val aliases = exportedProcedureTokens.map {
      case ((name, _), proc) => (renameMap(name), module) -> proc
    }

    newProcedureTokens ++ aliases
  }

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

  private def parseOne(tokenizer: TokenizerInterface, structureParser: StructureParser, source: String, filename: String, ownerModuleFilename: Option[String], oldResults: StructureResults): StructureResults = {
      val tokens =
        tokenizer.tokenizeString(source, filename)
          .filter(_.tpe != TokenType.Comment)
          .map(Namer0)
      structureParser.parse(tokens, ownerModuleFilename, oldResults, filename)
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
  def findImports(tokens: Iterator[Token]): Seq[Seq[String]] = {
    val reader = new SeqReader[Token](tokens.filter(_.tpe != TokenType.Comment).map(Namer0).to(LazyList), _.start)
    val combinators = new StructureCombinators

    combinators.program(reader) match {
      case combinators.Success(declarations, _) => {
        declarations.flatMap(x =>
          x match {
            case StructureDeclarations.Import(pathComponents, _, _, _) => Seq(pathComponents)
            case _ => Seq()
          })
      }
      case _ => Seq()
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

  def parse(tokens: Iterator[Token], module: Option[String], oldResults: StructureResults, filename: String): StructureResults =
    StructureCombinators.parse(tokens, filename) match {
      case Right(declarations) =>
        StructureChecker.rejectMisplacedDeclarations(declarations)
        StructureChecker.rejectMisplacedConstants(declarations)
        StructureChecker.rejectExportOutsideModule(declarations, module.isDefined)
        StructureChecker.rejectNonProceduresInModule(declarations, module.isDefined)
        StructureChecker.rejectDuplicateDeclarations(declarations)
        StructureChecker.rejectDuplicateNames(declarations,
          StructureParser.usedNames(
            oldResults.program, oldResults.procedures.filter{ case ((_, procModule), _) => procModule == module }))
        StructureChecker.rejectMissingReport(declarations)
        StructureConverter.convert(declarations, displayName, module,
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
