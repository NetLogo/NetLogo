// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Nobody, Syntax }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _nobody extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NobodyType)
  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context) =
    Nobody
}
