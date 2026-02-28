// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.api.DummyExtensionManager
import org.nlogo.compile.api.{ FrontMiddleBridgeInterface, ProcedureDefinition }
import org.nlogo.core.{ Femto, FrontEndInterface, FrontEndProcedure, Program }

object TestHelper {
  private val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")

  private val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")
  import scala.collection.immutable.ListMap
  // returns proccedures which have been fully compiled, but not yet optimized
  private[compile] def compiledProcedures(source: String, program: Program, is3D: Boolean = false): Seq[ProcedureDefinition] = {
    val oldProceduresListMap = ListMap[(String, Option[String]), FrontEndProcedure]()
    val oldNvmProceduresListMap = ListMap[(String, Option[String]), org.nlogo.nvm.Procedure]()
    val extensionManager = new DummyExtensionManager
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(source, None, program, false, oldProceduresListMap, extensionManager)
    val backifier = new Backifier(feStructureResults.program, extensionManager)
    bridge(feStructureResults, oldNvmProceduresListMap, topLevelDefs, backifier)
  }
}
