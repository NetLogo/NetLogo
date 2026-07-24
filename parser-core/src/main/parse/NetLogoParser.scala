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
    val nonAliasProcedures = structureResults.procedures.filter {
      case ((name, module), proc) => name == proc.name && module == proc.module
    }

    val allProcedures = oldProcedures ++ structureResults.procedures
    val newTopLevelProcedures = nonAliasProcedures -- oldProcedures.keys

    val topLevelDefs = newTopLevelProcedures.values.map { x =>
        val scope = StructureParser.usedNames(structureResults.program,
          allProcedures.filter { case ((_, module), _ ) => module == x.module },
          x.module.isDefined)
        parseProcedure(structureResults, scope, oldProcedures, extensionManager)(x)
      }.toSeq
    (topLevelDefs, structureResults)
  }

  def parseProcedure(
    structureResults:  StructureResults,
    globallyUsedNames: SymbolTable,
    oldProcedures:     ProceduresMap,
    extensionManager:  ExtensionManager)(procedure: FrontEndProcedure): ProcedureDefinition = {
    val rawTokens = structureResults.procedureTokens((procedure.name, procedure.module))
    val usedNames = globallyUsedNames.addSymbols(procedure.args, SymbolType.ProcedureVariable)
    val namedTokens = {
      val consolidatedTokens = ConsolidatingTokenStream(rawTokens.iterator, ScopedIdentifierConsolidator)
      val letNamedTokens = TransformableTokenStream(consolidatedTokens, LetNamer)
      val namer = new Namer(
          structureResults.program
        , oldProcedures ++ structureResults.procedures
        , procedure
        , extensionManager
        )
      namer.validateProcedure()
      TransformableTokenStream(letNamedTokens, namer)
    }
    ExpressionParser(procedure, namedTokens, usedNames)
  }

  def findProcedurePositions(source: String, dialectOption: Option[Dialect]): Map[String, ProcedureSyntax] = {
    val dialect = dialectOption.getOrElse(NetLogoCore)
    val tokens = tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, new DummyExtensionManager))
    StructureParser.findProcedurePositions(tokens.toSeq)
  }
}
