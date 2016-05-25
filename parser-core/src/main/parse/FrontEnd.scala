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

class FrontEnd extends FrontEndMain
    with FrontEndInterface

trait FrontEndMain {

  import FrontEndInterface.{ ProceduresMap, FrontEndResults }
  import FrontEnd.tokenizer

  // entry points

  def frontEnd(compilationOperand: CompilationOperand): FrontEndResults = {
    import compilationOperand.{ extensionManager, oldProcedures }
    val structureResults = StructureParser.parseSources(tokenizer, compilationOperand)
    def parseProcedure(procedure: FrontEndProcedure): ProcedureDefinition = {
      val rawTokens = structureResults.procedureTokens(procedure.name)
      val usedNames =
        StructureParser.usedNames(structureResults.program,
          oldProcedures ++ structureResults.procedures, Seq()) ++
        procedure.args.map(_ -> SymbolType.LocalVariable)
      // on LetNamer vs. Namer vs. LetScoper, see comments in LetScoper
      val namedTokens = {
        val letNamedTokens = LetNamer(rawTokens.iterator)
        val namer =
          new Namer(structureResults.program,
            oldProcedures ++ structureResults.procedures,
            extensionManager)
        val namedTokens = namer.process(letNamedTokens, procedure)
        val letScoper = new LetScoper(usedNames)
        letScoper(namedTokens.buffered)
      }
      ExpressionParser(procedure, namedTokens)
    }
    val newTopLevelProcedures = (structureResults.procedures -- oldProcedures.keys)

    var topLevelDefs = newTopLevelProcedures.values.map(parseProcedure).toSeq

    topLevelDefs = transformers.foldLeft(topLevelDefs) {
      case (defs, transform) => defs.map(transform.visitProcedureDefinition)
    }

    val letVerifier = new LetVerifier
    topLevelDefs.foreach(letVerifier.visitProcedureDefinition)

    new AgentTypeChecker(topLevelDefs).check()  // catch agent type inconsistencies

    val verifier = new TaskVariableVerifier
    topLevelDefs.foreach(verifier.visitProcedureDefinition)

    val cfVerifier = new ControlFlowVerifier
    val verifiedDefs = topLevelDefs.map(cfVerifier.visitProcedureDefinition)

    (verifiedDefs, structureResults)
  }

  private def transformers: Seq[AstTransformer] = {
    Seq(
      new TaskSpecializer,
      new TaskVariableVerifier,
      new LetReducer,
      new CarefullyVisitor
    )
  }

  def tokenizeForColorization(source: String, dialect: Dialect, extensionManager: ExtensionManager): Seq[core.Token] = {
    tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, extensionManager)).toSeq
  }

  def findProcedurePositions(source: String, dialectOption: Option[Dialect]): Map[String, ProcedureSyntax] = {
    val dialect = dialectOption.getOrElse(NetLogoCore)
    val tokens = tokenizer.tokenizeString(source).map(Namer.basicNamer(dialect, new DummyExtensionManager))
    StructureParser.findProcedurePositions(tokens.toSeq)
  }

  def findIncludes(source: String): Seq[String] = {
    val tokens = tokenizer.tokenizeString(source)
    StructureParser.findIncludes(tokens)
  }
}
