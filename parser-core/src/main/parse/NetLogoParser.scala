// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ CompilationOperand, Dialect, DummyExtensionManager, ExtensionManager,
  FrontEndInterface, FrontEndProcedure, NetLogoCore, ProcedureDefinition,
  ProcedureSyntax, StructureResults, TokenizerInterface },
  FrontEndInterface.ProceduresMap


// trait for methods shared by FrontEnd and AstRewriter, both of which parse NetLogo code
trait NetLogoParser {
  def tokenizer: TokenizerInterface

  def basicParse(compilationOperand: CompilationOperand): (Seq[ProcedureDefinition], StructureResults) = {
    import compilationOperand.{ extensionManager, oldProcedures }
    val structureResults = StructureParser.parseSources(tokenizer, compilationOperand)
    val globallyUsedNames =
      StructureParser.usedNames(structureResults.program,
        oldProcedures ++ structureResults.procedures)

    val newTopLevelProcedures = (structureResults.procedures -- oldProcedures.keys)

    val topLevelDefs = newTopLevelProcedures.values
      .map(parseProcedure(structureResults, globallyUsedNames, oldProcedures, extensionManager)).toSeq
    (topLevelDefs, structureResults)
  }

  def parseProcedure(
    structureResults:  StructureResults,
    globallyUsedNames: SymbolTable,
    oldProcedures:     ProceduresMap,
    extensionManager:  ExtensionManager)(procedure: FrontEndProcedure): ProcedureDefinition = {
    val rawTokens = structureResults.procedureTokens(procedure.name)
    val usedNames = globallyUsedNames.addSymbols(procedure.args, SymbolType.LocalVariable)
    // on LetNamer vs. Namer vs. LetScoper, see comments in LetScoper
    val namedTokens = {
      val letNamedTokens = TransformableTokenStream(rawTokens.iterator, LetNamer)
      val namer =
        new Namer(structureResults.program,
          oldProcedures ++ structureResults.procedures,
          procedure, extensionManager)
      namer.validateProcedure()
      val namedTokens = TransformableTokenStream(letNamedTokens, namer)
      val letScoper = new LetScoper(usedNames, namedTokens)
      // we map unknown idents to symbols and ExpressionParser errors as appropriate
      val letScopedStream = TransformableTokenStream(namedTokens, letScoper)
      letScopedStream
    }
    ExpressionParser(procedure, namedTokens, usedNames)
  }

  def findProcedurePositions(source: String, dialectOption: Option[Dialect]): Map[String, ProcedureSyntax] = {
    val dialect = dialectOption.getOrElse(NetLogoCore)
    val tokens = tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, new DummyExtensionManager))
    StructureParser.findProcedurePositions(tokens.toSeq)
  }
}
