// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _anyother extends Reporter {

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, argEvalAgentSet(context, 0)))

  def report_1(context: Context, sourceSet: AgentSet): Boolean = {
    val it = sourceSet.iterator
    while(it.hasNext) {
      if (it.next() ne context.agent)
        return true
    }
    false
  }

}
