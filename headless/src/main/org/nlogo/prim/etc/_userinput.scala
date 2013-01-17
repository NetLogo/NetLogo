// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, I18N, ReporterRunnable, Syntax }
import org.nlogo.nvm.{ Context, EngineException, HaltException, Reporter }

class _userinput extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.StringType)

  override def report(context: Context) = {
    val message = Dump.logoObject(args(0).report(context))
    workspace.updateUI(context)
    val result =
      workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run() =
            workspace.userInput(message)
        })
    result.getOrElse(throw new HaltException(true))
  }

}
