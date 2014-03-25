// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, Version }
import org.nlogo.nvm.{ Context, Reporter }

class _netlogoversion extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    Version.versionNumberOnly
}
