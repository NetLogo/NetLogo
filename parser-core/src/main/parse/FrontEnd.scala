// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{AstTransformer, CompilationOperand, Dialect, Femto,
    ExtensionManager, FrontEndInterface, CompilerException,
    Token, TokenizerInterface }
import java.text.CharacterIterator

object FrontEnd extends FrontEnd {
  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")
  val tokenMapper = new TokenMapper
}

trait FrontEnd extends FrontEndMain
  with FrontEndInterface

trait FrontEndMain extends NetLogoParser {

  import FrontEndInterface.FrontEndResults

  // entry points

  def frontEnd(compilationOperand: CompilationOperand): FrontEndResults = {
    val (rawProcDefs, structureResults) = basicParse(compilationOperand)

    val topLevelDefs = transformers(compilationOperand).foldLeft(rawProcDefs) {
      case (defs, transform) =>
        val newDefs = defs.map(transform.visitProcedureDefinition)
        newDefs
    }

    val letVerifier = new LetVerifier
    topLevelDefs.foreach(letVerifier.visitProcedureDefinition)

    new AgentTypeChecker(topLevelDefs).check()  // catch agent type inconsistencies

    val cfVerifier = new ControlFlowVerifier
    val verifiedDefs = topLevelDefs.map(cfVerifier.visitProcedureDefinition)

    (verifiedDefs, structureResults)
  }

  private def transformers(compilationOperand: CompilationOperand): Seq[AstTransformer] = {
    Seq(new LetReducer, new CarefullyVisitor, new ClosureTagger, new SourceTagger(compilationOperand))
  }

  def tokenizeStringConsolidated(source: String): Iterator[core.Token] = {
    ConsolidatingTokenStream(tokenizer.tokenizeString(source), ScopedIdentifierConsolidator)
  }

  def tokenizeForColorization(source: String, dialect: Dialect, extensionManager: ExtensionManager): Seq[core.Token] = {
    tokenizeStringConsolidated(source).map(Namer.basicNamer(dialect, extensionManager)).toSeq
  }

  def tokenizeForColorizationIterator(source: String, dialect: Dialect, extensionManager: ExtensionManager): Iterator[core.Token] = {
    tokenizeStringConsolidated(source).map(Namer.basicNamer(dialect, extensionManager))
  }

  def tokenizeWithWhitespace(source: String, dialect: Dialect,
                             extensionManager: ExtensionManager): Iterator[core.Token] =
    tokenizer.tokenizeWithWhitespace(source, null).map(Namer.basicNamer(dialect, extensionManager))


  // Unlike tokenizeWithWhitespace(), this doesn't use the Namer. This is because it's used by NetLogoTokenMaker, and
  // it's doing the naming itself. The name should probably be changed so that this is clear though.
  // -Kritphong M October 2025
  def tokenizeWithWhitespaceConsolidated(iter: CharacterIterator, filename: String): Iterator[Token] ={
    val tokens = tokenizer.tokenizeWithWhitespace(iter, filename)
    ConsolidatingTokenStream(tokens, ScopedIdentifierConsolidator)
  }

  @throws(classOf[CompilerException])
  def findIncludes(source: String): Seq[String] = {
    // The tokenizing and parsing just for the `__includes` declaration is quite slow on large (5000+ line) models that
    // do *not* have an `__includes`.  Maybe there is a better way to handle it by storing that info when the file is
    // parsed/checked but for now, we have a workaround of just doing a quick regex to check if `__includes` is there
    // before bothering with the tokenizing and parsing.  -Jeremy B November 2020
    if (FrontEndInterface.hasIncludes(source)) {
      val tokens = tokenizer.tokenizeString(source)
      StructureParser.findIncludes(tokens)
    } else {
      Seq()
    }
  }

  def findExtensions(source: String): Seq[String] =
    StructureParser.findExtensions(tokenizer.tokenizeString(source))

  @throws(classOf[CompilerException])
  def findImports(source: String): Seq[(Option[String], String)] = {
    if (FrontEndInterface.hasImport(source)) {
      val tokens = tokenizer.tokenizeString(source)
      StructureParser.findImports(tokens)
    } else {
      Seq()
    }
  }
}
