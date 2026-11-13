// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.Dump
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _with extends Reporter {

  override def report(context: Context): AgentSet =
    report_1(context, argEvalAgentSet(context, 0), args(1))

  def report_1(context: Context, sourceSet: AgentSet, reporterBlock: Reporter): AgentSet = {
    val freshContext = new Context(context, sourceSet)
    val result = collection.mutable.ArrayBuffer[Agent]()
    reporterBlock.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.iterator
    while(iter.hasNext) {
      val tester = iter.next()
      freshContext.evaluateReporter(tester, reporterBlock) match {
        case b: java.lang.Boolean =>
          if (b.booleanValue)
            result += tester
        case x =>
          val message = loc"""|$displayName expected a true/false value from ${Dump.logoObject(tester)}, but got
                              |${Dump.logoObject(x)} instead."""

          throw new RuntimePrimitiveException(context, this, message)
      }
    }
    AgentSet.fromArray(sourceSet.kind, result.toArray)
  }

}
