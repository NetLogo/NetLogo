// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm

class _checksum extends nvm.Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType, "O---")
  override def report(context: nvm.Context) =
    workspace.worldChecksum + "\n" + workspace.graphicsChecksum
}
