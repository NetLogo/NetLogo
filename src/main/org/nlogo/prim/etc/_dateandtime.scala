package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _time extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    new java.text.SimpleDateFormat("hh:mm:ss.SSS a dd-MMM-yyyy")
      .format(new java.util.Date)
}
