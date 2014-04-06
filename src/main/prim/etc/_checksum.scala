// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm

class _checksum extends nvm.Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType,
      agentClassString = "O---")
  override def report(context: nvm.Context): String =
    workspace.worldChecksum + "\n" + workspace.graphicsChecksum
}
