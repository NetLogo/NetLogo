// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.ReporterRunnable
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _userfile extends Reporter {

  override def report(context: Context): AnyRef = {
    workspace.updateUI()
    val result: Option[String] =
      workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run() =
            workspace.userFile
        })
    result match {
      case None =>
        java.lang.Boolean.FALSE
      case Some(path) =>
        if (!new java.io.File(path).exists)
          throw new RuntimePrimitiveException(
            context, this, "This file doesn't exist")
        path
    }
  }

}
