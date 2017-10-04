// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{ CompilationEnvironment, CompilerException, CompilerUtilitiesInterface, Dialect, Femto, FrontEndInterface, ProcedureSyntax, Program, Token }
import org.nlogo.api.{ SourceOwner, World }
import org.nlogo.nvm.{ PresentationCompilerInterface, CompilerFlags, CompilerResults, ImportHandler, Procedure }
import org.nlogo.api.ExtensionManager

import scala.collection.immutable.ListMap

// This is intended to be called from Java as well as Scala, so @throws declarations are included.
// No other classes in this package are public. - ST 2/20/08, 4/9/08, 1/21/09

class Compiler(val dialect: Dialect) extends PresentationCompilerInterface {

  val defaultDialect = dialect

  val utilities =
    Femto.scalaSingleton[CompilerUtilitiesInterface]("org.nlogo.parse.CompilerUtilities")

  def frontEnd =
    Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

  // tokenizer singletons
  val parserTokenizer = Femto.scalaSingleton[org.nlogo.core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

  // some private helpers
  private type ProceduresMap = ListMap[String, Procedure]
  private val noProcedures: ProceduresMap = ListMap.empty[String, Procedure]

  // used to compile the Code tab, including declarations
  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults = {
    val (procedures, newProgram) =
      CompilerMain.compile(Map("" -> source), None, program, false, noProcedures, extensionManager, compilationEnv)

    new CompilerResults(procedures, newProgram)
  }

  // used to compile the Code tab with additional sources
  // (like system dynamics modeler)
  @throws(classOf[CompilerException])
  def compileProgram(source: String, additionalSources: Seq[SourceOwner], program: Program, extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults = {
    val sources =
      Map("" -> source) ++ additionalSources.map(additionalSource =>
          additionalSource.classDisplayName -> additionalSource.innerSource).toMap

    val (procedures, newProgram) =
      CompilerMain.compile(sources, None, program, false, noProcedures, extensionManager, compilationEnv)

    new CompilerResults(procedures, newProgram)
  }

  //NOTE: This doesn't actually pay attention to flags, at the moment
  def compileProgram(
    source:                 String,
    program:                Program,
    extensionManager:       ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    flags:                  CompilerFlags): CompilerResults = {
      compileProgram(source, Seq(), program, extensionManager, compilationEnvironment)
  }

  def makeLiteralReporter(value: AnyRef): org.nlogo.nvm.Reporter =
    Literals.makeLiteralReporter(value)

  // used to compile a single procedures only, from outside the Code tab
  @throws(classOf[CompilerException])
  def compileMoreCode(
    source:           String,
    displayName:      Option[String],
    program:          Program,
    oldProcedures:    ProceduresMap,
    extensionManager: ExtensionManager,
    compilationEnv:   CompilationEnvironment): CompilerResults = {
    val (procedures, newProgram) =
      CompilerMain.compile(Map("" -> source),displayName,program,true,oldProcedures,extensionManager,compilationEnv)
    new CompilerResults(procedures, newProgram)
  }

  //NOTE: This doesn't actually pay attention to flags, at the moment
  def compileMoreCode(
    source:                 String,
    displayName:            Option[String],
    program:                Program,
    oldProcedures:          Procedure.ProceduresMap,
    extensionManager:       ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    flags:                  CompilerFlags): CompilerResults = {
      compileMoreCode(source, displayName, program, oldProcedures, extensionManager, compilationEnvironment)
  }

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

  // this function tries to go as far as possible, but throws an exception if there is
  // a syntax error. It assumes that any unrecognized tokens are unknown variables.
  // The FrontEnd is currently not quite forgiving enough, but we will use it for the moment.
  // Additionally, the compiler doesn't currently work for 3D prims, so that will also need to be fixed.
  // this also always parses, which probably isn't desirable, but we don't have an option at this point
  @throws(classOf[CompilerException])
  private def checkSyntax(source: String, subprogram: Boolean, program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean) {

    val oldProceduresListMap = ListMap[String, Procedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, None, program, subprogram, oldProceduresListMap, extensionManager)
  }

  /// TODO: remove all direct dependencies on world by having below methods take an ImportHandler
  //  instead of World and ExtensionManager - RG 10/29/15

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef =
    utilities.readFromString(source)

  // will probably need a way to determine the 3D-ness of the current language, not worried about that at the moment
  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readFromString(source, literalImportHandler)
  }

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): java.lang.Double = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readNumberFromString(source, literalImportHandler)
  }

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: ExtensionManager): AnyRef = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readFromFile(currFile, literalImportHandler)
  }

  // used for procedures menu
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
    frontEnd.findProcedurePositions(source, Some(dialect))

  // used for includes menu
  def findIncludes(sourceFileName: String, source: String,
    compilationEnvironment: CompilationEnvironment): Option[Map[String, String]] = {
    val includes = frontEnd.findIncludes(source)
    if (includes.isEmpty) { // this allows the includes menu to be displayed for __includes []
      parserTokenizer.tokenizeString(source)
        .find(t => t.text.equalsIgnoreCase("__includes"))
        .map(_ => Map.empty[String, String])
    } else
      Some((includes zip includes.map(compilationEnvironment.resolvePath)).toMap)
  }

  // used by VariableNameEditor
  // it *shouldn't* matter whether we're in 3D mode or not because
  // the tokenizer makes no effort to match commands and reporters at this stage
  def isValidIdentifier(s: String) =
    parserTokenizer.isValidIdentifier(s)

  // used by CommandLine
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager) = {
    val proceduresListMap = ListMap[String, Procedure](procedures.toSeq: _*)
    utilities.isReporter(s, program, proceduresListMap, extensionManager)
  }

  // used by the indenter. we always use the 2D tokenizer since it doesn't matter in this context
  def getTokenAtPosition(source: String, position: Int): Token =
    parserTokenizer.getTokenAtPosition(source, position).orNull

  // this is for the syntax-highlighting editor
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token] =
    frontEnd.tokenizeForColorization(source, defaultDialect, extensionManager).toArray
  def tokenizeForColorizationIterator(source: String, extensionManager: ExtensionManager): Iterator[Token] =
    frontEnd.tokenizeForColorizationIterator(source, defaultDialect, extensionManager)

}
