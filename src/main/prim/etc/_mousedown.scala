// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Reporter }

class _mousedown extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(workspace.mouseDown)
}
