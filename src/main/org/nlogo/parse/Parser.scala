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

trait Parser extends nvm.ParserInterface {

  import nvm.ParserInterface.ProceduresMap
  import Parser.tokenizer

  // entry points

  def frontEnd(source: String, oldProcedures: ProceduresMap = nvm.ParserInterface.NoProcedures, program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], StructureResults) =
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
      val identifiedTokens =
        namer.process(rawTokens.iterator, procedure)  // resolve references
      new ExpressionParser(procedure, taskNumbers)
        .parse(identifiedTokens) // parse
    }
    val procDefs = structureResults.procedures.values.flatMap(parseProcedure).toVector
    if (frontEndOnly)  // for Tortoise
      for(procdef <- procDefs)
        procdef.accept(new SetVisitor)
    (procDefs, structureResults)
  }

  ///

  // In the following 3 methods, the initial call to NumberParser is a performance optimization.
  // During import-world, we're calling readFromString over and over again and most of the time
  // the result is a number.  So we try the fast path through NumberParser first before falling
  // back to the slow path where we actually tokenize. - ST 4/7/11

  def readFromString(source: String): AnyRef =
    api.NumberParser.parse(source).right.getOrElse(
      new parse0.LiteralParser(null, null, null)
        .getLiteralValue(tokenizer.tokenize(source)))

  def readFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): AnyRef = {
    api.NumberParser.parse(source).right.getOrElse(
      Parser.literalParser(world, extensionManager)
        .getLiteralValue(tokenizer.tokenize(source)))
  }

  def readNumberFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): java.lang.Double =
    api.NumberParser.parse(source).right.getOrElse(
      Parser.literalParser(world, extensionManager)
        .getNumberValue(tokenizer.tokenize(source)))

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: api.File, world: api.World, extensionManager: api.ExtensionManager): AnyRef = {
    val tokens: Iterator[api.Token] =
      new parse0.TokenReader(currFile, tokenizer)
    val result =
      Parser.literalParser(world, extensionManager)
        .getLiteralFromFile(tokens)
    // now skip whitespace, so that the model can use file-at-end? to see whether there are any
    // more values left - ST 2/18/04
    // org.nlogo.util.File requires us to maintain currFile.pos ourselves -- yuck!!! - ST 8/5/04
    var done = false
    while(!done) {
      currFile.reader.mark(1)
      currFile.pos += 1
      val i = currFile.reader.read()
      if(i == -1 || !Character.isWhitespace(i)) {
        currFile.reader.reset()
        currFile.pos -= 1
        done = true
      }
    }
    result
  }

  // used by CommandLine
  def isReporter(s: String, program: api.Program, procedures: ProceduresMap, extensionManager: api.ExtensionManager) =
    try {
      val results =
        new StructureParser(tokenizer.tokenize("to __is-reporter? report " + s + "\nend"),
                            None, StructureResults(program, procedures))
          .parse(subprogram = true)
      val namer =
        new Namer(program, procedures ++ results.procedures, extensionManager, Vector())
      val proc = results.procedures.values.head
      val tokens = namer.process(results.tokens(proc).iterator, proc)
      tokens.toStream
        .drop(1)  // skip _report
        .map(_.tpe)
        .dropWhile(_ == api.TokenType.OpenParen)
        .headOption
        .exists(reporterTokenTypes)
    }
    catch { case _: api.CompilerException => false }

  private val reporterTokenTypes: Set[api.TokenType] = {
    import api.TokenType._
    Set(OpenBracket, Literal, Ident, Reporter)
  }

}
