// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.nvm.{ Context, HaltException, Reporter }

class _useroneof extends Reporter {

  override def report(context: Context): AnyRef = {
    val message = Dump.logoObject(args(0).report(context))
    val list = argEvalList(context, 1)
    workspace.updateUI()
    val choice =
      workspace.waitForResult(
        new ReporterRunnable[Option[AnyRef]] {
          override def run() =
            workspace.userOneOf(message, list)
        })
    choice.getOrElse(throw new HaltException(true))
  }

}
