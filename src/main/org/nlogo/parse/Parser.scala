// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ agent, api, nvm, parse0 }
import org.nlogo.util.Femto

object Parser extends Parser {
  val tokenizer: api.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")
  val tokenMapper = new parse0.TokenMapper(
    "/system/tokens.txt", "org.nlogo.prim.")
  // well this is pretty ugly.  LiteralParser and LiteralAgentParser call each other,
  // so they're hard to instantiate, but we "tie the knot" using lazy val. - ST 5/3/13
  def literalParser(world: api.World, extensionManager: api.ExtensionManager): parse0.LiteralParser = {
    lazy val literalParser =
      new parse0.LiteralParser(world, extensionManager, parseLiteralAgentOrAgentSet)
    lazy val parseLiteralAgentOrAgentSet: Iterator[api.Token] => AnyRef =
      new agent.LiteralAgentParser(
          world, literalParser.readLiteralPrefix _, Fail.cAssert _, Fail.exception _)
        .parseLiteralAgentOrAgentSet _
    literalParser
  }
}

class Parser extends ParserMain
    with nvm.ParserInterface with ParserExtras

trait ParserMain {

  import nvm.ParserInterface.ProceduresMap
  import Parser.tokenizer

  // entry points

  def frontEnd(source: String, oldProcedures: ProceduresMap = nvm.ParserInterface.NoProcedures,
      program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], StructureResults) =
    frontEndHelper(source, None, program, true,
      oldProcedures, new api.DummyExtensionManager, frontEndOnly = true)

  // used by Tortoise. bails after parsing so we can put a different back end on.
  // the frontEndOnly flag is currently just for Tortoise and can hopefully go away in the future.
  // Tortoise currently needs SetVisitor to happen even though SetVisitor is technically part of the
  // back end.  An example of how this might be redone in the future would be to fold the
  // functionality of SetVisitor into Namer. - ST 1/24/13
  def frontEndHelper(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager, frontEndOnly: Boolean = false)
    : (Seq[ProcedureDefinition], StructureResults) = {
    val structureResults = StructureParser.parseAll(
      tokenizer, source, displayName, program, subprogram, oldProcedures, extensionManager)
    val taskNumbers = Iterator.from(1)
    // the return type is plural because tasks inside a procedure get
    // lambda-lifted and become procedures themselves
    def parseProcedure(procedure: nvm.Procedure): Seq[ProcedureDefinition] = {
      val rawTokens = structureResults.tokens(procedure)
      val lets = {
        val used1 = structureResults.program.usedNames
        val used2 = (structureResults.procedures.keys ++ oldProcedures.keys).map(_ -> "procedure")
        val used3 = procedure.args.map(_ -> "local variable here")
        val used4 = StructureParser.alwaysUsedNames
        new parse0.LetScoper(rawTokens)
          .scan(used1 ++ used2 ++ used3 ++ used4)
      }
      val namer =
        new Namer(structureResults.program,
          oldProcedures ++ structureResults.procedures,
          extensionManager, lets)
      val namedTokens =
        new parse0.CountedIterator(
          namer.process(rawTokens.iterator, procedure))  // resolve references
      val stuffedTokens =
        namedTokens.map(LetStuffer.stuffLet(_, lets, namedTokens))
      new ExpressionParser(procedure, taskNumbers)
        .parse(stuffedTokens) // parse
    }
    val procDefs = structureResults.procedures.values.flatMap(parseProcedure).toVector
    if (frontEndOnly)  // for Tortoise
      for(procdef <- procDefs)
        procdef.accept(new SetVisitor)
    (procDefs, structureResults)
  }

}
