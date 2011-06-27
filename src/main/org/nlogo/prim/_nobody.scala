package org.nlogo.prim

import org.nlogo.api.Nobody
import org.nlogo.nvm.{Context, Pure, Reporter, Syntax}

class _nobody extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NOBODY)
  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context) =
    Nobody
}
