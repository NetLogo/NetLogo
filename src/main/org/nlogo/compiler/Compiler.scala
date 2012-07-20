// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{ CompilerException, ExtensionManager, NumberParser, Program, Token,
                       TokenizerInterface, TokenReaderInterface, TokenType, TokenMapperInterface, World }
import org.nlogo.nvm.{ CompilerInterface, CompilerResults, Procedure, Workspace }
import org.nlogo.util.Femto

// This is intended to be called from Java as well as Scala, so @throws declarations are included.
// No other classes in this package are public. - ST 2/20/08, 4/9/08, 1/21/09

object Compiler extends CompilerInterface {

  // tokenizer singletons
  val Tokenizer2D = Femto.scalaSingleton(classOf[TokenizerInterface], "org.nlogo.lex.Tokenizer2D")
  val TokenMapper2D = Femto.scalaSingleton(classOf[TokenMapperInterface], "org.nlogo.lex.TokenMapper2D")

  // some private helpers
  private type ProceduresMap = java.util.Map[String, Procedure]
  private val noProcedures: ProceduresMap = java.util.Collections.emptyMap[String, Procedure]
  private def tokenizer = Tokenizer2D

  // used to compile the Code tab, including declarations
  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager): CompilerResults =
    CompilerMain.compile(source, None, program, false, noProcedures, extensionManager)

  // used to compile a single procedures only, from outside the Code tab
  @throws(classOf[CompilerException])
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager): CompilerResults =
    CompilerMain.compile(source, displayName, program, true, oldProcedures, extensionManager)

  // these two used by input boxes
  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean) {
    checkSyntax("to __bogus-name " + source + "\nend",
                true, program, procedures, extensionManager, parse)
  }
  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean) {
    checkSyntax("to-report __bogus-name report " + source + "\nend",
                true, program, procedures, extensionManager, parse)
  }

  // like in the auto-converter we want to compile as far as we can but
  // we assume that any tokens we don't recognize are actually globals
  // that we don't know about.
  @throws(classOf[CompilerException])
  private def checkSyntax(source: String, subprogram: Boolean, program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean) {
    implicit val t = tokenizer
    val results = new StructureParser(t.tokenizeRobustly(source), None,
                                      program, oldProcedures, extensionManager)
      .parse(subprogram)
    val identifierParser = new IdentifierParser(program, noProcedures, results.procedures, !parse)
    import collection.JavaConverters._  // results.procedures.values is a java.util.Collection
    for(procedure <- results.procedures.values.asScala) {
      val tokens = identifierParser.process(results.tokens(procedure).iterator, procedure)
      if(parse)
        new ExpressionParser(procedure).parse(tokens)
    }
  }

  ///

  /// TODO: There are a few places below where we downcast api.World to agent.World in order to pass
  /// it to ConstantParser.  This should really be cleaned up so that ConstantParser uses api.World
  /// too. - ST 2/23/09

  // In the following 3 methods, the initial call to NumberParser is a performance optimization.
  // During import-world, we're calling readFromString over and over again and most of the time
  // the result is a number.  So we try the fast path through NumberParser first before falling
  // back to the slow path where we actually tokenize. - ST 4/7/11

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser().getConstantValue(tokenizer.tokenize(source).iterator))

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser(world.asInstanceOf[org.nlogo.agent.World], extensionManager)
        .getConstantValue(tokenizer.tokenize(source).iterator))

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): java.lang.Double =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser(world.asInstanceOf[org.nlogo.agent.World], extensionManager)
      .getNumberValue(tokenizer.tokenize(source).iterator))

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef = {
    val tokens: Iterator[Token] =
      Femto.get(classOf[TokenReaderInterface], "org.nlogo.lex.TokenReader",
                Array(currFile, tokenizer))
    val result = new ConstantParser(world.asInstanceOf[org.nlogo.agent.World], extensionManager)
      .getConstantFromFile(tokens)
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

  // used for procedures menu
  def findProcedurePositions(source: String): java.util.Map[String, java.util.List[AnyRef]] =
    new StructureParserExtras()(tokenizer).findProcedurePositions(source)

  // used for includes menu
  def findIncludes(sourceFileName: String, source: String): java.util.Map[String, String] =
    new StructureParserExtras()(tokenizer).findIncludes(sourceFileName, source)

  // used by VariableNameEditor
  def isValidIdentifier(s: String) = tokenizer.isValidIdentifier(s)

  // used by CommandLine
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager) =
    try {
      implicit val t = tokenizer
      val results =
        new StructureParser(t.tokenize("to __is-reporter? report " + s + "\nend"),
                            None, program, procedures, extensionManager)
          .parse(subprogram = true)
      val identifierParser =
        new IdentifierParser(program, procedures, results.procedures, forgiving = false)
      import collection.JavaConverters._  // results.procedures.values is a java.util.Collection
      val proc = results.procedures.values.asScala.head
      val tokens = identifierParser.process(results.tokens(proc).iterator, proc)
      tokens
        .tail  // skip _report
        .map(_.tyype)
        .dropWhile(_ == TokenType.OPEN_PAREN)
        .headOption
        .exists(reporterTokenTypes)
    }
    catch { case _: CompilerException => false }

  private val reporterTokenTypes: Set[TokenType] = {
    import TokenType._
    Set(OPEN_BRACKET, CONSTANT, IDENT, REPORTER, VARIABLE)
  }

  // used by the indenter. we always use the 2D tokenizer since it doesn't matter in this context
  def getTokenAtPosition(source: String, position: Int): Token =
    tokenizer.getTokenAtPosition(source, position)

  // this is for the syntax-highlighting editor
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token] =
    tokenizer.tokenizeForColorization(source, extensionManager)

}
