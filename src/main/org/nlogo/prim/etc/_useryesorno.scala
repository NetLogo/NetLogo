// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, I18N, ReporterRunnable, Syntax }
import org.nlogo.nvm.{ Context, EngineException, HaltException, Reporter }

class _useryesorno extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)

  override def report(context: Context): java.lang.Boolean = {
    val message = Dump.logoObject(args(0).report(context))
    workspace.updateUI(context)
    val result =
      workspace.waitForResult(
        new ReporterRunnable[Option[Boolean]] {
          override def run =
            workspace.userYesOrNo(message)
        })
    result.map(Boolean.box).getOrElse(
      throw new HaltException(true))
  }

}
