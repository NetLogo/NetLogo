// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api, nvm, parse },
  api.{ CompilerException, ExtensionManager, NumberParser, Program, Token,
        TokenReaderInterface, TokenType, World },
  nvm.{ CompilerInterface, ParserInterface, CompilerFlags, CompilerResults, Procedure, Workspace },
  ParserInterface.ProceduresMap,
  org.nlogo.util.Femto

object Compiler extends parse.Parser with CompilerInterface {

  // used to compile the Code tab, including declarations
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager, flags: CompilerFlags): CompilerResults =
    compile(source, None, program, false, ParserInterface.NoProcedures, extensionManager, flags)

  // used to compile a single procedures only, from outside the Code tab
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: ProceduresMap, extensionManager: ExtensionManager, flags: CompilerFlags): CompilerResults =
    compile(source, displayName, program, true, oldProcedures, extensionManager, flags)

  private def compile(source: String, displayName: Option[String], program: Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: ExtensionManager,
      flags: CompilerFlags): CompilerResults = {
    val (defs, structureResults) =
      frontEndHelper(source, displayName, program, subprogram, oldProcedures, extensionManager)
    BackEnd.backEnd(defs, structureResults.program, source, extensionManager.profilingEnabled, flags)
  }

}
