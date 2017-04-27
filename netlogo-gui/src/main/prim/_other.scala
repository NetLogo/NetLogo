// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{Agent, AgentSet, LazyAgentSet}
import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.nvm.{Context, Reporter}
import org.nlogo.nvm.RuntimePrimitiveException
import java.util.ArrayList

class _other extends Reporter {

  def report(context: Context): AgentSet = {
    report_1(context, argEvalAgentSet(context, 0))
  }

  def report_1(context: Context, sourceSet: AgentSet): AgentSet = {
    if (sourceSet.isInstanceOf[LazyAgentSet]) {
      sourceSet.asInstanceOf[LazyAgentSet].lazyOther(context.agent)
      sourceSet
    } else {
//      new LazyAgentSet(null, sourceSet, context.agent :: Nil)
      val others = new ArrayList[Agent]
      others.add(context.agent)
      new LazyAgentSet(null, sourceSet, others)
    }
  }
}
