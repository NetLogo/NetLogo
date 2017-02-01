// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{AstTransformer, CompilationOperand, CompilationEnvironment, Dialect, DummyCompilationEnvironment, Femto,
    ExtensionManager, DummyExtensionManager, NetLogoCore, FrontEndInterface, FrontEndProcedure,
    Program, TokenizerInterface, ProcedureDefinition, ProcedureSyntax}

object FrontEnd extends FrontEnd {
  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")
  val tokenMapper = new core.TokenMapper("/system/tokens-core.txt", "org.nlogo.core.prim.")
}

trait FrontEnd extends FrontEndMain
  with FrontEndInterface

trait FrontEndMain extends NetLogoParser {

  import FrontEndInterface.{ ProceduresMap, FrontEndResults }

  // entry points

  def frontEnd(compilationOperand: CompilationOperand): FrontEndResults = {
    import compilationOperand.{ extensionManager, oldProcedures }
    val (rawProcDefs, structureResults) = basicParse(compilationOperand)

    val topLevelDefs = transformers.foldLeft(rawProcDefs) {
      case (defs, transform) => defs.map(transform.visitProcedureDefinition)
    }


    val letVerifier = new LetVerifier
    topLevelDefs.foreach(letVerifier.visitProcedureDefinition)

    new AgentTypeChecker(topLevelDefs).check()  // catch agent type inconsistencies

    val cfVerifier = new ControlFlowVerifier
    val verifiedDefs = topLevelDefs.map(cfVerifier.visitProcedureDefinition)

    (verifiedDefs, structureResults)
  }

  private def transformers: Seq[AstTransformer] = {
    Seq(new LetReducer, new CarefullyVisitor, new ClosureTagger)
  }

  def tokenizeForColorization(source: String, dialect: Dialect, extensionManager: ExtensionManager): Seq[core.Token] = {
    tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, extensionManager)).toSeq
  }

  def tokenizeForColorizationIterator(source: String, dialect: Dialect, extensionManager: ExtensionManager): Iterator[core.Token] = {
    tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, extensionManager))
  }

  def findIncludes(source: String): Seq[String] = {
    val tokens = tokenizer.tokenizeString(source)
    StructureParser.findIncludes(tokens)
  }
}
