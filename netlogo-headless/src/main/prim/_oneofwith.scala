// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Dump
import org.nlogo.core.{ LocaleContext, Nobody }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _oneofwith extends Reporter {

  override def report(context: Context): AnyRef =
    report_1(context, argEvalAgentSet(context, 0), args(1))

  def report_1(context: Context, sourceSet: AgentSet, arg1: Reporter): AnyRef = {
    val freshContext = new Context(context, sourceSet)
    arg1.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.shufflerator(context.job.random)
    while(iter.hasNext) {
      val tester = iter.next()
      freshContext.evaluateReporter(tester, arg1) match {
        case b: java.lang.Boolean =>
          if (b)
            return tester
        case x =>
          val message = loc"""|WITH expected a true/false value from ${Dump.logoObject(tester)},
                              |but got ${Dump.logoObject(x)} instead."""

          throw new RuntimePrimitiveException(context, this, message)
      }
    }
    Nobody
  }

}
