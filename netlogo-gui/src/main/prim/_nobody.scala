// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

class _nobody extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context) =
    Nobody
}
