// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, nvm, parse0 }

trait ParserExtras { this: nvm.ParserInterface =>

  import nvm.ParserInterface.ProceduresMap
  import Parser.tokenizer

  // In the following 3 methods, the initial call to NumberParser is a performance optimization.
  // During import-world, we're calling readFromString over and over again and most of the time
  // the result is a number.  So we try the fast path through NumberParser first before falling
  // back to the slow path where we actually tokenize. - ST 4/7/11

  def readFromString(source: String): AnyRef =
    api.NumberParser.parse(source).right.getOrElse(
      new parse0.LiteralParser(null, null, null)
        .getLiteralValue(tokenizer.tokenize(source)
          .map(parse0.Namer0)))

  def readFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): AnyRef =
    api.NumberParser.parse(source).right.getOrElse(
      Parser.literalParser(world, extensionManager)
        .getLiteralValue(tokenizer.tokenize(source)
          .map(parse0.Namer0)))

  def readNumberFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): java.lang.Double =
    api.NumberParser.parse(source).right.getOrElse(
      Parser.literalParser(world, extensionManager)
        .getNumberValue(tokenizer.tokenize(source)
          .map(parse0.Namer0)))

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: api.File, world: api.World, extensionManager: api.ExtensionManager): AnyRef = {
    val tokens: Iterator[api.Token] =
      new parse0.TokenReader(currFile, tokenizer)
        .map(parse0.Namer0)
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

  def makeLiteralReporter(value: AnyRef): nvm.Reporter =
    ExpressionParser.makeLiteralReporter(value)

  // used by CommandLine
  def isReporter(s: String, program: api.Program, procedures: ProceduresMap, extensionManager: api.ExtensionManager) =
    try {
      val results =
        new StructureParser(tokenizer.tokenize("to __is-reporter? report " + s + "\nend").map(parse0.Namer0),
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
