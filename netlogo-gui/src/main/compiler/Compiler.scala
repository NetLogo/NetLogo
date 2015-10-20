// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{ NumberParser, TokenizerInterface,
                        TokenReaderInterface, TokenMapperInterface, World }
import org.nlogo.core.CompilerUtilitiesInterface
import org.nlogo.core.FrontEndInterface
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.core.Token
import org.nlogo.core.TokenType
import org.nlogo.nvm.{ CompilerInterface, CompilerResults, Procedure, Workspace }
import org.nlogo.core.CompilationEnvironment
import org.nlogo.core.ExtensionManager
import org.nlogo.util.Femto

import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._

// This is intended to be called from Java as well as Scala, so @throws declarations are included.
// No other classes in this package are public. - ST 2/20/08, 4/9/08, 1/21/09

object Compiler extends CompilerInterface {

  val compilerUtilities = Femto.scalaSingleton(classOf[CompilerUtilitiesInterface], "org.nlogo.parse.CompilerUtilities")
  private val frontEnd = Femto.scalaSingleton(classOf[FrontEndInterface], "org.nlogo.parse.FrontEnd")

  // tokenizer singletons
  val Tokenizer2D = Femto.scalaSingleton(classOf[TokenizerInterface], "org.nlogo.lex.Tokenizer2D")
  val Tokenizer3D = Femto.scalaSingleton(classOf[TokenizerInterface], "org.nlogo.lex.Tokenizer3D")
  val TokenMapper2D = Femto.scalaSingleton(classOf[TokenMapperInterface], "org.nlogo.lex.TokenMapper2D")

  // some private helpers
  private type ProceduresMap = java.util.Map[String, Procedure]
  private val noProcedures: ProceduresMap = java.util.Collections.emptyMap[String, Procedure]
  private def tokenizer(is3D: Boolean) = if(is3D) Tokenizer3D else Tokenizer2D

  // used to compile the Code tab, including declarations
  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults = {
    val (procedures, newProgram) =
      CompilerMain.compile(source, None, program, false, noProcedures, extensionManager, compilationEnv)

    new CompilerResults(procedures, newProgram)
  }

  // used to compile a single procedures only, from outside the Code tab
  @throws(classOf[CompilerException])
  def compileMoreCode(source:String,displayName: Option[String], program:Program,oldProcedures:ProceduresMap,extensionManager:ExtensionManager, compilationEnv:CompilationEnvironment):CompilerResults = {
    val (procedures, newProgram) = CompilerMain.compile(source,displayName,program,true,oldProcedures,extensionManager,compilationEnv)
    new CompilerResults(procedures, newProgram)
  }

  // these two used by input boxes
  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment) {
    checkSyntax("to __bogus-name " + source + "\nend",
                true, program, procedures, extensionManager, parse, compilationEnv)
  }
  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment) {
    checkSyntax("to-report __bogus-name report " + source + "\nend",
                true, program, procedures, extensionManager, parse, compilationEnv)
  }

  // this function tries to go as far as possible, but throws an exception if there is
  // a syntax error. It assumes that any unrecognized tokens are unknown variables.
  // The FrontEnd is currently not quite forgiving enough, but we will use it for the moment.
  // Additionally, the compiler doesn't currently work for 3D prims, so that will also need to be fixed.
  // this also always parses, which probably isn't desirable, but we don't have an option at this point
  @throws(classOf[CompilerException])
  private def checkSyntax(source: String, subprogram: Boolean, program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment) {

    implicit val t = tokenizer(program.dialect.is3D)
    val oldProceduresListMap = ListMap[String, Procedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, None, program, subprogram, oldProceduresListMap, extensionManager)
  }

  def autoConvert(source: String, subprogram: Boolean, reporter: Boolean, version: String, w: AnyRef, ignoreErrors: Boolean, is3D: Boolean): String = {
    // well, this typecast is gruesome, but I really want to put CompilerInterface in
    // api not nvm, and since AutoConverter2 is a grotesque hack anyway and can probably
    // go away after 4.1, we'll just do it... - ST 2/23/09
    val workspace = w.asInstanceOf[Workspace]
    // AutoConverter1 handles the easy conversions
    new AutoConverter1()(tokenizer(is3D)).convert(source, subprogram, reporter, version)
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
  def readFromString(source: String, is3D: Boolean): AnyRef =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser().getConstantValue(tokenizer(is3D).tokenize(source).iterator))

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ExtensionManager, is3D: Boolean): AnyRef =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser(world.asInstanceOf[org.nlogo.agent.World], extensionManager)
        .getConstantValue(tokenizer(is3D).tokenize(source).iterator))

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager, is3D: Boolean): java.lang.Double =
    NumberParser.parse(source).right.getOrElse(
      new ConstantParser(world.asInstanceOf[org.nlogo.agent.World], extensionManager)
      .getNumberValue(tokenizer(is3D).tokenize(source).iterator))

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: ExtensionManager): AnyRef = {
    val tokens: Iterator[Token] =
      Femto.get(classOf[TokenReaderInterface], "org.nlogo.lex.TokenReader",
                Array(currFile, tokenizer(world.program.dialect.is3D)))
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
  def findProcedurePositions(source: String, is3D: Boolean): java.util.Map[String, java.util.List[AnyRef]] =
    new StructureParserExtras()(tokenizer(is3D)).findProcedurePositions(source)

  // used for includes menu
  def findIncludes(sourceFileName: String, source: String, is3D: Boolean): Option[java.util.Map[String, String]] =
    new StructureParserExtras()(tokenizer(is3D)).findIncludes(sourceFileName, source)

  // used by VariableNameEditor
  def isValidIdentifier(s: String, is3D: Boolean) = tokenizer(is3D).isValidIdentifier(s)

  // used by CommandLine
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment) = {
    // this will definitely need a way to pass in whether compilation is 2D or 3D
    val proceduresListMap = ListMap[String, Procedure](procedures.toSeq: _*)
    compilerUtilities.isReporter(s, program, proceduresListMap, extensionManager)
  }

  private val reporterTokenTypes: Set[TokenType] = {
    import TokenType._
    Set(OpenBracket, Literal, Extension, Reporter, Ident)
  }

  // used by the indenter. we always use the 2D tokenizer since it doesn't matter in this context
  def getTokenAtPosition(source: String, position: Int): Token =
    tokenizer(false).getTokenAtPosition(source, position)

  // this is for the syntax-highlighting editor
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager, is3D: Boolean): Array[Token] =
    tokenizer(is3D).tokenizeForColorization(source, extensionManager)

}
