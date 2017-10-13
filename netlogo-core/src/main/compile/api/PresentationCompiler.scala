// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import java.lang.{ Double => JDouble }

import org.nlogo.{ core, api => nlapi, nvm },
  core.{ CompilerException, CompilationEnvironment,
    CompilerUtilitiesInterface, Dialect, Femto, FrontEndInterface, FrontEndProcedure,
    ProcedureSyntax, Program, Token, TokenizerInterface },
    FrontEndInterface.ProceduresMap,
  nlapi.{ ExtensionManager, World },
  nvm.ImportHandler

import scala.collection.immutable.ListMap

trait PresentationCompiler {
  def frontEnd: FrontEndInterface
  def defaultDialect: Dialect
  def dialect: Dialect
  val utilities =
    Femto.scalaSingleton[CompilerUtilitiesInterface]("org.nlogo.parse.CompilerUtilities")

  val parserTokenizer = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

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

  // used for procedures menu
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
    frontEnd.findProcedurePositions(source, Some(dialect))

  // used by the indenter. we always use the 2D tokenizer since it doesn't matter in this context
  def getTokenAtPosition(source: String, position: Int): Token =
    parserTokenizer.getTokenAtPosition(source, position).orNull

  // used by CommandLine
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager) = {
    val proceduresListMap = ListMap[String, FrontEndProcedure](procedures.toSeq: _*)
    utilities.isReporter(s, program, proceduresListMap, extensionManager)
  }

  // used by VariableNameEditor
  // it *shouldn't* matter whether we're in 3D mode or not because
  // the tokenizer makes no effort to match commands and reporters at this stage
  def isValidIdentifier(s: String) =
    parserTokenizer.isValidIdentifier(s)

  /// TODO: remove all direct dependencies on world by having below methods take an ImportHandler
  //  instead of World and ExtensionManager - RG 10/29/15
  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef =
    utilities.readFromString(source)


  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readFromString(source, literalImportHandler)
  }

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): JDouble = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readNumberFromString(source, literalImportHandler)
  }

  // this is for the syntax-highlighting editor
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token] =
    frontEnd.tokenizeForColorization(source, defaultDialect, extensionManager).toArray
  def tokenizeForColorizationIterator(source: String, extensionManager: ExtensionManager): Iterator[Token] =
    frontEnd.tokenizeForColorizationIterator(source, defaultDialect, extensionManager)

  // this function tries to go as far as possible, but throws an exception if there is
  // a syntax error. It assumes that any unrecognized tokens are unknown variables.
  // The FrontEnd is currently not quite forgiving enough, but we will use it for the moment.
  // Additionally, the compiler doesn't currently work for 3D prims, so that will also need to be fixed.
  // this also always parses, which probably isn't desirable, but we don't have an option at this point
  @throws(classOf[CompilerException])
  private def checkSyntax(source: String, subprogram: Boolean, program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager, parse: Boolean) {

    val oldProceduresListMap = ListMap[String, FrontEndProcedure](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, None, program, subprogram, oldProceduresListMap, extensionManager)
  }
}
