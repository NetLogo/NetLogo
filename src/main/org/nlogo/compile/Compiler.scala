// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api, nvm, parse },
  nvm.ParserInterface.{ ProceduresMap, NoProcedures }

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

object Compiler extends parse.Parser with nvm.CompilerInterface {

  // used to compile the Code tab, including declarations
  def compileProgram(source: String, program: api.Program,
      extensionManager: api.ExtensionManager, flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, None, program, false, NoProcedures, extensionManager, flags)

  // used to compile a single procedures only, from outside the Code tab
  def compileMoreCode(source: String, displayName: Option[String], program: api.Program,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager,
      flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, displayName, program, true, oldProcedures, extensionManager, flags)

  private def compile(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager,
      flags: nvm.CompilerFlags): nvm.CompilerResults = {
    val (defs, structureResults) =
      frontEndHelper(source, displayName, program, subprogram, oldProcedures, extensionManager)
    middle.MiddleEnd.middleEnd(defs, flags)
    back.BackEnd.backEnd(defs, structureResults.program, source, extensionManager.profilingEnabled, flags)
  }

}
