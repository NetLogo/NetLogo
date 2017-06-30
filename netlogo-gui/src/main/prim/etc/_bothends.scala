// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.nvm.{ Context, Reporter }

class _bothends extends Reporter {

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    context.agent.asInstanceOf[Link].bothEnds
}
