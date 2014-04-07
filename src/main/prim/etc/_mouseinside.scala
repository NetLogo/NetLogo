// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _mouseinside extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(workspace.mouseInside)
}
