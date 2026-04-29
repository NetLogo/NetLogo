// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import java.lang.{ Double => JDouble }

import org.nlogo.api.{ ExtensionManager, LibraryManager, SourceOwner, World }
import org.nlogo.compile.api.{ BackEndInterface, CommandMunger, FrontMiddleBridgeInterface, MiddleEndInterface,
                               Optimizations, ReporterMunger }
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, CompilerUtilitiesInterface, Dialect, Femto,
                        FrontEndInterface, NetLogoCore, ProcedureSyntax, Program, Token, TokenizerInterface,
                        TokenType }
import org.nlogo.nvm.{ CompilerFlags, CompilerResults, ImportHandler, Optimizations => NvmOptimizations,
                       PresentationCompilerInterface, Procedure, Reporter },
  Procedure.{ ProceduresMap, NoProcedures }

import scala.collection.immutable.ListMap

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

object Compiler extends Compiler(NetLogoCore)

class Compiler(dialect: Dialect) extends PresentationCompilerInterface {
  override val defaultDialect = dialect

  override val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")
  override val utilities = Femto.scalaSingleton[CompilerUtilitiesInterface](
    "org.nlogo.parse.CompilerUtilities")

  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")
  val middleEnd = Femto.scalaSingleton[MiddleEndInterface](
    "org.nlogo.compile.middle.MiddleEnd")
  val backEnd = Femto.scalaSingleton[BackEndInterface](
    "org.nlogo.compile.back.BackEnd")

  private val parserTokenizer = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def backifier(program: Program, extensionManager: ExtensionManager) =
    new Backifier(program, extensionManager)

  // used to compile the Code tab, including declarations
  override def compileProgram(source: String, program: Program, extensionManager: ExtensionManager,
    libManager: LibraryManager, compilationEnvironment: CompilationEnvironment,
    shouldAutoInstallLibs: Boolean, flags: CompilerFlags): CompilerResults =
    compile( source, None, program, false, NoProcedures, extensionManager
           , libManager, compilationEnvironment, shouldAutoInstallLibs, flags)

  // used to compile a single procedures only, from outside the Code tab
  override def compileMoreCode(source: String, displayName: Option[String], program: Program,
      oldProcedures: ProceduresMap, extensionManager: ExtensionManager,
      libManager: LibraryManager, compilationEnvironment: CompilationEnvironment,
      flags: CompilerFlags): CompilerResults =
    compile( source, displayName, program, true, oldProcedures, extensionManager, libManager
           , compilationEnvironment, false, flags)

  private def compile(source: String, displayName: Option[String], oldProgram: Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: ExtensionManager,
      libManager: LibraryManager, compilationEnvironment: CompilationEnvironment,
      shouldAutoInstallLibs: Boolean, flags: CompilerFlags): CompilerResults = {
    val (topLevelDefs, structureResults) =
      frontEnd.frontEnd( source, displayName, oldProgram, subprogram, oldProcedures
                       , extensionManager, libManager, compilationEnvironment, shouldAutoInstallLibs)
    val bridged = bridge(structureResults, oldProcedures, topLevelDefs, backifier(structureResults.program, extensionManager))
    val allDefs = middleEnd.middleEnd(
      bridged,
      structureResults.program,
      Map("" -> source),
      compilationEnvironment,
      getOptimizations(flags))
    backEnd.backEnd(allDefs, structureResults.program, compilationEnvironment.profilingEnabled, flags)
  }

  val defaultCompilerFlags =
    CompilerFlags(optimizations = NvmOptimizations.headlessOptimizations)

  override def compileProgram( source: String, additionalSources: Seq[SourceOwner], program: Program
                             , extensionManager: ExtensionManager, libManager: LibraryManager
                             , compilationEnv: CompilationEnvironment, shouldAutoInstallLibs: Boolean
                             ): CompilerResults = {
    val allSources =
      Map("" -> source) ++ additionalSources.map(additionalSource => additionalSource.classDisplayName -> additionalSource.innerSource).toMap
    val (topLevelDefs, structureResults) =
      frontEnd.frontEnd(
        CompilationOperand( allSources, extensionManager, libManager, compilationEnv, program
                          , Procedure.NoProcedures, subprogram = false
                          , shouldAutoInstallLibs = shouldAutoInstallLibs)
      )
    val bridged = bridge(structureResults, Procedure.NoProcedures, topLevelDefs, backifier(structureResults.program, extensionManager))
    val allDefs = middleEnd.middleEnd(
      bridged,
      structureResults.program,
      allSources,
      compilationEnv,
      getOptimizations(defaultCompilerFlags))
    backEnd.backEnd(allDefs, structureResults.program, compilationEnv.profilingEnabled, CompilerFlags())
  }

  private def getOptimizations(flags: CompilerFlags): Optimizations =
    if (flags.useOptimizer)
      flags.optimizations.foldLeft(Optimizations.none) {
        case (opts, (NvmOptimizations.Reporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.scalaSingleton[ReporterMunger](klass) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.DialectReporter, klass)) =>
          opts.copy(reporterOptimizations = Femto.get[ReporterMunger](klass, NetLogoCore) +: opts.reporterOptimizations)
        case (opts, (NvmOptimizations.Command, klass)) =>
          opts.copy(commandOptimizations = Femto.scalaSingleton[CommandMunger](klass) +: opts.commandOptimizations)
      }
    else
      Optimizations.none

  override def makeLiteralReporter(value: AnyRef): Reporter =
    Literals.makeLiteralReporter(value)

  // i don't think any of the following methods are used in headless, but some extensions can't be run in headless mode
  // if this class doesn't conform to PresentationCompilerInterface, which defines these methods. sources are copied
  // from the version of this class in netlogo-gui. (Isaac B 9/27/25)

  override def checkCommandSyntax(source: String, program: Program, procedures: ListMap[Tuple2[String, Option[String]], Procedure],
                                  extensionManager: ExtensionManager, parse: Boolean,
                                  compilationEnv: CompilationEnvironment): Unit = {
    checkSyntax("to __bogus-name " + source + "\nend", program, procedures, extensionManager, parse, compilationEnv)
  }

  override def checkReporterSyntax(source: String, program: Program, procedures: ListMap[Tuple2[String, Option[String]], Procedure],
                                   extensionManager: ExtensionManager, parse: Boolean,
                                   compilationEnv: CompilationEnvironment): Unit = {
    checkSyntax("to-report __bogus-name report " + source + "\nend", program, procedures, extensionManager, parse,
                compilationEnv)
  }

  private def checkSyntax(source: String, program: Program, procedures: ListMap[Tuple2[String, Option[String]], Procedure],
                          extensionManager: ExtensionManager, parse: Boolean,
                          compilationEnv: CompilationEnvironment): Unit = {
    frontEnd.frontEnd(source, None, program, true, ListMap[Tuple2[String, Option[String]], Procedure](procedures.toSeq*),
                      extensionManager)
  }

  override def findIncludes(sourceFileName: String, source: String,
                            compilationEnv: CompilationEnvironment): Option[Map[String, String]] = {
    val includes = frontEnd.findIncludes(source)

    if (includes.isEmpty) {
      if (!FrontEndInterface.hasIncludes(source)) {
        None
      } else {
        parserTokenizer.tokenizeString(source).find(t => t.text.equalsIgnoreCase("__includes")).map(_ => Map())
      }
    } else {
      Some(includes.zip(includes.map(compilationEnv.resolvePath)).toMap)
    }
  }

  override def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
    frontEnd.findProcedurePositions(source, Some(dialect))

  override def getTokenAtPosition(source: String, position: Int): Token =
    parserTokenizer.getTokenAtPosition(source, position).orNull

  override def isReporter(source: String, program: Program, procedures: ListMap[Tuple2[String, Option[String]], Procedure],
                          extensionManager: ExtensionManager, compilationEnv: CompilationEnvironment): Boolean =
    utilities.isReporter(source, program, ListMap[Tuple2[String, Option[String]], Procedure](procedures.toSeq*), extensionManager)

  override def isValidIdentifier(source: String, extensionManager: ExtensionManager): Boolean = {
    frontEnd.tokenizeForColorizationIterator(source, dialect, extensionManager).takeWhile(_.tpe != TokenType.Eof)
            .filter(_.tpe == TokenType.Ident).size == 1
  }

  override def readFromString(source: String): AnyRef =
    utilities.readFromString(source)

  override def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef =
    utilities.readFromString(source, new ImportHandler(world, extensionManager))

  override def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): JDouble =
    utilities.readNumberFromString(source, new ImportHandler(world, extensionManager))

  override def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token] =
    frontEnd.tokenizeForColorization(source, dialect, extensionManager).toArray

  override def tokenizeForColorizationIterator(source: String, extensionManager: ExtensionManager): Iterator[Token] =
    frontEnd.tokenizeForColorizationIterator(source, dialect, extensionManager)

  override def tokenizeWithWhitespace(source: String, extensionManager: ExtensionManager): Iterator[Token] =
    frontEnd.tokenizeWithWhitespace(source, dialect, extensionManager)

}
