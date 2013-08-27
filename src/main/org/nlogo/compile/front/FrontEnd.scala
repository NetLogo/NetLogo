// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.{ agent, api, nvm, parse }
import org.nlogo.util.Femto

object FrontEnd extends FrontEnd {
  val tokenizer: api.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")
  val tokenMapper = new parse.TokenMapper(
    "/system/tokens.txt", "org.nlogo.prim.")
  // well this is pretty ugly.  LiteralParser and LiteralAgentParser call each other,
  // so they're hard to instantiate, but we "tie the knot" using lazy val. - ST 5/3/13
  def literalParser(world: api.World, extensionManager: api.ExtensionManager): parse.LiteralParser = {
    lazy val literalParser =
      new parse.LiteralParser(world, extensionManager, parseLiteralAgentOrAgentSet)
    lazy val parseLiteralAgentOrAgentSet: Iterator[api.Token] => AnyRef =
      new agent.LiteralAgentParser(
          world, literalParser.readLiteralPrefix _, Fail.cAssert _, Fail.exception _)
        .parseLiteralAgentOrAgentSet _
    literalParser
  }
}

class FrontEnd extends FrontEndMain
    with FrontEndInterface with FrontEndExtras

trait FrontEndMain {

  import nvm.FrontEndInterface.ProceduresMap
  import FrontEnd.tokenizer

  // entry points

  def frontEnd(source: String, oldProcedures: ProceduresMap = nvm.FrontEndInterface.NoProcedures,
      program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], nvm.StructureResults) =
    frontEndHelper(source, None, program, true,
      oldProcedures, new api.DummyExtensionManager)

  def frontEndHelper(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager)
    : (Seq[ProcedureDefinition], nvm.StructureResults) = {
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
        new parse.LetScoper(rawTokens)
          .scan(used1 ++ used2 ++ used3 ++ used4)
      }
      val namer =
        new Namer(structureResults.program,
          oldProcedures ++ structureResults.procedures,
          extensionManager, lets)
      val namedTokens =
        new parse.CountedIterator(
          namer.process(rawTokens.iterator, procedure))  // resolve references
      val stuffedTokens =
        namedTokens.map(LetStuffer.stuffLet(_, lets, namedTokens))
      new ExpressionParser(procedure, taskNumbers)
        .parse(stuffedTokens) // parse
    }
    val procDefs = structureResults.procedures.values.flatMap(parseProcedure).toVector
    (procDefs, structureResults)
  }

}
