// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _optimizecount(operator: (Int, Int) => Boolean) extends Reporter {

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      report_1(context, argEvalAgentSet(context, 0), argEvalDoubleValue(context, 1)))

  def report_1(context: Context, sourceSet: AgentSet, checkValue: Double): Boolean = {
    sourceSet.checkCount(checkValue.toInt, operator)
  }
}
