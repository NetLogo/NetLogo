// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Nobody
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _nobody extends Reporter with Pure {
  override def report(context: Context): Nobody.type =
    report_1(context)
  def report_1(context: Context): Nobody.type =
    Nobody
}
