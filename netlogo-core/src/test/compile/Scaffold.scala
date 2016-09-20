// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{Femto, FrontEndInterface}
import org.nlogo.{ api => nlogoApi, nvm },
  nlogoApi.DummyExtensionManager
import org.nlogo.compile.api.{ FrontMiddleBridgeInterface, ProcedureDefinition }

object Scaffold {

  val frontEnd = Femto.scalaSingleton[FrontEndInterface](
    "org.nlogo.parse.FrontEnd")
  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")

  def apply(source: String): Seq[ProcedureDefinition] = {
    val (coreDefs, results) = frontEnd.frontEnd(source)
    bridge(
      results,
      nvm.Procedure.NoProcedures,
      coreDefs,
      new Backifier(results.program, new DummyExtensionManager)
    )
  }

}
