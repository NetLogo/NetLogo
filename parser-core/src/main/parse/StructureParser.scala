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
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, CompilerException, ErrorSource, I18N, Import,
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
              parseOne(tokenizer, structureParser, source, filename, None, results)
          }

        if (subprogram)
          firstResults
        else {
          // Check for duplicate import statements. We're doing this check instead of relying on the duplicate symbol
          // check so that we can provide a more specific error message.
          val (maybeDuplicateImport, _) = firstResults.imports.foldLeft((None: Option[Import], Set())) {
            case ((None, previousNames), x) => {
              val key = (x.packageName, x.moduleName)
              val maybeDuplicate = if (previousNames.contains(key)) Some(x) else None
              (maybeDuplicate, previousNames + key)
            }

            // No need to update previousNames now that we've found something
            case ((token @ Some(_), previousNames), _) => (token, previousNames)
          }

          maybeDuplicateImport.foreach(x => exception(I18N.errors.get("compiler.StructureParser.importMultipleImports"), x.token))

          var processedImports: Set[String] = Set()

          Iterator.iterate(firstResults) { results =>
            var newResults: StructureResults = results

            // Handle imports
            if (newResults.imports.nonEmpty) {
              val previousResults = newResults
              val currentImport = results.imports.head
              val separator = System.getProperty("file.separator")

              val suppliedPath = compilationEnvironment.resolveModule(currentImport.filename, currentImport.packageName, currentImport.moduleName)

              val currentModule = for {
                pathString <- currentImport.filename
                basename = pathString.split(separator).last.toUpperCase()
              } yield raw".NLS$$".r.replaceFirstIn(basename, "")

              newResults = includeFile(compilationEnvironment, suppliedPath) match {
                case Some((path, fileContents)) =>
                  parseOne(tokenizer, structureParser, fileContents, path, Some(currentImport.moduleName),
                    newResults.copy(imports = newResults.imports.tail,
                      includedSources = newResults.includedSources :+ suppliedPath))
                case None =>
                  exception(I18N.errors.getN("compiler.StructureParser.importNotFound", suppliedPath), currentImport.token)
              }

              if (processedImports.contains(currentImport.moduleName)) {
                exception(I18N.errors.getN("compiler.StructureParser.importLoop", currentImport.moduleName), currentImport.token)
              } else {
                processedImports += currentImport.moduleName
              }

              if (newResults.program != previousResults.program) {
                exception(I18N.errors.getN("compiler.StructureParser.importContainsNonProcedure"), currentImport.token)
              }

              val prefix = currentImport.alias.getOrElse(currentImport.moduleName) + ":"
              val exportedNames =
                newResults.`export`.map(_.exportedNames.toSet).getOrElse(newResults.procedures.keys.map(_._1).toSet)
              val newProcedures = addProcedureAliases(
                previousResults.procedures,
                newResults.procedures,
                exportedNames,
                currentModule,
                prefix
              )
              val newProcedureTokens = addProcedureTokenAliases(
                previousResults.procedureTokens,
                newResults.procedureTokens,
                exportedNames,
                currentModule,
                currentImport.filename,
                prefix
              )

              newResults = newResults.copy(
                program = firstResults.program, // Exclude globals, breeds, and breed variables in modules
                procedures = newProcedures,
                procedureTokens = newProcedureTokens
              )
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
    exportedNames: Set[String],
    module: Option[String],
    prefix: String): ProceduresMap = {

    val changedProcedures = newProcedures.removedAll(oldProcedures.keys)
    val exportedProcedures = changedProcedures.filter{case ((name, _), _) =>
      exportedNames.contains(name)
    }

    val aliases = exportedProcedures.map{case ((name, _), proc) =>
      val key = (prefix.toUpperCase + name, module)
      proc.aliases = proc.aliases :+ key
      key -> proc}

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
    oldProcedureTokens: Map[(String, Option[String]), Iterable[Token]],
    newProcedureTokens: Map[(String, Option[String]), Iterable[Token]],
    exportedNames: Set[String],
    module: Option[String],
    filename: Option[String],
    prefix: String): Map[(String, Option[String]), Iterable[Token]] = {

    val changedProcedureTokens = newProcedureTokens.removedAll(oldProcedureTokens.keys)
    val exportedProcedureTokens = changedProcedureTokens.filter{case ((name, _), _) =>
      exportedNames.contains(name)
    }

    // addProcedureAliases() already checks for name conflicts, so no need to check again here.
    val aliases = exportedProcedureTokens.map{case ((name, _), proc) =>
      (prefix.toUpperCase + name, module) -> proc
    }

    newProcedureTokens ++ aliases
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

  private def parseOne(tokenizer: TokenizerInterface, structureParser: StructureParser, source: String, filename: String, module: Option[String], oldResults: StructureResults): StructureResults = {
      val tokens =
        tokenizer.tokenizeString(source, filename)
          .filter(_.tpe != TokenType.Comment)
          .map(Namer0)
      structureParser.parse(tokens, module, oldResults, filename)
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
  def findImports(tokens: Iterator[Token]): Seq[(Option[String], String)] = {
    val importPositionedTokens =
      tokens.dropWhile(! _.text.equalsIgnoreCase("import"))
    val result =
      if (importPositionedTokens.isEmpty)
        Seq()
      else {
        importPositionedTokens.next()
        val importWithoutComments = importPositionedTokens.filter(_.tpe != TokenType.Comment)
        if (importWithoutComments.next().tpe != TokenType.OpenBracket)
          exception("Did not find expected open bracket for import declaration", tokens.next())
        else {
          val nameTokens = importWithoutComments
            .takeWhile((x) => x.tpe != TokenType.OpenBracket && x.tpe != TokenType.CloseBracket)
            .filter(_.tpe == TokenType.Ident)
            .map(_.value.toString)
            .toSeq
          nameTokens.length match {
            case 1 => Seq((None, nameTokens.head))
            case 2 => Seq((Some(nameTokens.head), nameTokens(1)))
            case _ => exception("Malformed import", tokens.next()) // Malformed import
          }
        }
      }
    if (result.isEmpty) {
      result
    } else {
      result ++ findImports(tokens)
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
        StructureChecker.rejectMisplacedConstants(declarations)
        StructureChecker.rejectDuplicateDeclarations(declarations)
        StructureChecker.rejectDuplicateNames(declarations,
          StructureParser.usedNames(
            oldResults.program, oldResults.procedures.filter{case ((_, procModule), _) => procModule == module}))
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
