// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Nobody, Pure }
import org.nlogo.nvm.{ Context, Reporter }

class _nobody extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context) =
    Nobody
}
