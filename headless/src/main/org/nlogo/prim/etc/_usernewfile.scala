// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ ReporterRunnable, Syntax }
import org.nlogo.nvm.{ Context, EngineException, HaltException, Reporter }

class _usernewfile extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType | Syntax.BooleanType)

  override def report(context: Context): AnyRef = {
    if (workspace.getIsApplet)
      throw new EngineException(
        context, this, "You cannot choose a file from an applet.")
    workspace.updateUI()
    val result: Option[String] =
      workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run() =
            workspace.userNewFile
        })
    result.getOrElse(java.lang.Boolean.FALSE)
  }

}
