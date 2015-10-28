// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.TokenizerInterface
import org.nlogo.core.Program
import org.nlogo.core.{ Femto, FrontEndInterface, FrontEndProcedure, DummyExtensionManager, DummyCompilationEnvironment, Token }
import org.nlogo.nvm.Procedure

object TestHelper {
  private val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")

  import scala.collection.immutable.ListMap
  // returns proccedures which have been fully compiled, but not yet optimized
  private[compiler] def compiledProcedures(source: String, program: Program, is3D: Boolean = false): Seq[ProcedureDefinition] = {
    val oldProceduresListMap = ListMap[String, FrontEndProcedure]()
    val oldNvmProceduresListMap = ListMap[String, org.nlogo.nvm.Procedure]()
    val extensionManager = new DummyExtensionManager
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, None, program, false, oldProceduresListMap, extensionManager)
    val defs = CompilerBridge(feStructureResults, extensionManager, oldNvmProceduresListMap, topLevelDefs)
    defs
  }
}
