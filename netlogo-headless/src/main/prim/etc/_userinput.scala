// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.nvm.{ Context, HaltException, Reporter }

class _userinput extends Reporter {

  override def report(context: Context): String = {
    val message = Dump.logoObject(args(0).report(context))
    workspace.updateUI()
    val result =
      workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run() =
            workspace.userInput(message)
        })
    result.getOrElse(throw new HaltException(true))
  }

}
