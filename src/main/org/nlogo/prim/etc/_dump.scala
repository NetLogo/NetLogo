// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _dump extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType, "O---")
  override def report(context: Context): String =
    world.program.dump + "\n" +
    workspace.procedures.values
      .map(_.dump)
      .mkString("", "\n", "\n")
}
