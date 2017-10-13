// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{ CompilationEnvironment, CompilerException, Dialect, Femto, FrontEndInterface, Program }
import org.nlogo.api.{ SourceOwner, World }
import org.nlogo.nvm.{ PresentationCompilerInterface, CompilerFlags, CompilerResults, ImportHandler, Procedure }
import org.nlogo.api.ExtensionManager

import scala.collection.immutable.ListMap

// This is intended to be called from Java as well as Scala, so @throws declarations are included.
// No other classes in this package are public. - ST 2/20/08, 4/9/08, 1/21/09

class Compiler(val dialect: Dialect)
extends PresentationCompilerInterface
with api.PresentationCompiler {

  val defaultDialect = dialect

  def frontEnd =
    Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

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


  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: ExtensionManager): AnyRef = {
    val literalImportHandler = new ImportHandler(world, extensionManager)
    utilities.readFromFile(currFile, literalImportHandler)
  }
}
