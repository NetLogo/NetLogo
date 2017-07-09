// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.ReporterRunnable
import org.nlogo.nvm.{ Context, Reporter }

class _usernewfile extends Reporter {

  override def report(context: Context): AnyRef = {
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
