// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Reporter }

class _dump1 extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    context.activation.procedure.dump
}
