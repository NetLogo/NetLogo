// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _dumpextensions extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    workspace.getExtensionManager.dumpExtensions
}
