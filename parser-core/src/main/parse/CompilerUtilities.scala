// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{Dialect, DummyExtensionManager, CompilerException, CompilerUtilitiesInterface,
        Femto, File, FrontEndInterface, ExtensionManager, StructureResults, Program,
        LiteralImportHandler, TokenColorizer }

object CompilerUtilities extends CompilerUtilitiesInterface {
  import FrontEndInterface.ProceduresMap
  import FrontEnd.tokenizer

  def literalParser(importHandler: LiteralImportHandler): LiteralParser =
    new LiteralParser(importHandler)

  // In the following methods, the initial call to NumberParser is a performance optimization.
  // During import-world, we're calling readFromString over and over again and most of the time
  // the result is a number.  So we try the fast path through NumberParser first before falling
  // back to the slow path where we actually tokenize. - ST 4/7/11
  def readFromString(source: String): AnyRef =
    numberOrElse[AnyRef](source, NullImportHandler, _.getLiteralValue)

  def readNumberFromString(source: String): java.lang.Double =
    numberOrElse[java.lang.Double](source, NullImportHandler, _.getNumberValue)

  def readFromString(source: String, importHandler: LiteralImportHandler): AnyRef =
    numberOrElse[AnyRef](source, importHandler, _.getLiteralValue)

  def readNumberFromString(source: String, importHandler: LiteralImportHandler): java.lang.Double =
    numberOrElse[java.lang.Double](source, importHandler, _.getNumberValue)

  private def numberOrElse[A >: java.lang.Double](source: String, importHandler: LiteralImportHandler,
                                                  parseProcedure: => LiteralParser => Iterator[core.Token] => A): A =
    core.NumberParser.parse(source).right.getOrElse(
      parseProcedure(literalParser(importHandler))(
        tokenizer.tokenizeString(source).map(Namer0)))

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: File, importHandler: LiteralImportHandler): AnyRef = {
    val tokens: Iterator[core.Token] =
      new TokenReader(currFile, tokenizer)
        .map(Namer0)
    val result =
      literalParser(importHandler)
        .getLiteralFromFile(tokens)
    result
  }

  def isReporter(s: String): Boolean =
    isReporter(s, Program.empty(), FrontEndInterface.NoProcedures, new DummyExtensionManager)

  // used by CommandLine
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager) =
    try {
      val namedTokens =
        tokenizer.tokenizeString("to __is-reporter? report " + s + "\nend")
          .map(Namer0)
      val sp =
        new StructureParser(None, true)
      val results = sp.parse(namedTokens, StructureResults(program, procedures))
      val namer =
        new Namer(program, procedures ++ results.procedures, extensionManager)
      val proc = results.procedures.values.head
      val tokens = namer.process(results.procedureTokens(proc.name).iterator, proc)
      tokens.toStream
        .drop(1)  // skip _report
        .dropWhile(_.tpe == core.TokenType.OpenParen)
        .headOption
        .exists(isReporterToken)
    }
    catch { case _: CompilerException => false }

  private def isReporterToken(token: core.Token): Boolean = {
    import core.TokenType._
    token.tpe match {
      case OpenBracket | Literal | Ident =>
        true
      case Reporter =>
        ! (token.value.isInstanceOf[core.prim._letvariable] ||
          token.value.isInstanceOf[core.prim._symbol] ||
          token.value.isInstanceOf[core.prim._unknownidentifier])
      case _ =>
        false
    }
  }

  val colorizer: TokenColorizer = Colorizer
}
