// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm

class _checksum extends nvm.Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.StringType, "O---")
  override def report(context: nvm.Context): String =
    workspace.worldChecksum + "\n" + workspace.graphicsChecksum
}
