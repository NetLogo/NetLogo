// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import java.util.ArrayList

import org.nlogo.agent.{Agent, AgentSet, LazyAgentSet}
import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.nvm.{Context, Instruction, Reporter}
import org.nlogo.nvm.RuntimePrimitiveException

class _with extends Reporter {

  def report(context: Context): AgentSet = {
    val sourceSet = argEvalAgentSet(context, 0)
    val reporterBlock = args(1)
    report_1(context, sourceSet, reporterBlock)
  }

  def report_1(context: Context, sourceSet: AgentSet, reporterBlock: Reporter): AgentSet = {
    val freshContext = new Context(context, sourceSet)
    reporterBlock.checkAgentSetClass(sourceSet, context)

    val filter = new WithFunction(freshContext, this, displayName, reporterBlock)

    if (sourceSet.isInstanceOf[LazyAgentSet]) {
      sourceSet.asInstanceOf[LazyAgentSet].lazyWith(filter)
      sourceSet
    } else {
//      val withs = new ArrayList[(Agent) => Boolean]()
//      withs.add(filter)
//      new LazyAgentSet(null, sourceSet, withs = withs)
      new LazyAgentSet(null, sourceSet, withs = List(filter))
    }
  }
}

class WithFunction(freshContext: Context, instruction: Instruction, displayName: String, reporterBlock: Reporter)
  extends scala.Function1[Agent, Boolean] {
  override def apply(agent: Agent): Boolean =
    freshContext.evaluateReporter(agent, reporterBlock) match {
      case b: java.lang.Boolean => b.booleanValue
      case x => throw new RuntimePrimitiveException(freshContext, instruction, I18N.errors.getN(
        "org.nlogo.prim.$common.expectedBooleanValue",
        displayName, Dump.logoObject(agent), Dump.logoObject(x)))
    }
}
