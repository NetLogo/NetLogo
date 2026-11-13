// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Dump
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _anywith extends Reporter {

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      report_1(context, argEvalAgentSet(context, 0), args(1)))

  def report_1(context: Context, sourceSet: AgentSet, arg1: Reporter): Boolean = {
    val freshContext = new Context(context, sourceSet)
    arg1.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.iterator
    while(iter.hasNext) {
      val tester = iter.next()
      freshContext.evaluateReporter(tester, arg1) match {
        case b: java.lang.Boolean =>
          if (b)
            return true
        case x =>
          val message = loc"""|WITH expected a true/false value from ${Dump.logoObject(tester)},
                              |but got ${Dump.logoObject(x)} instead."""

          throw new RuntimePrimitiveException(context, this, message)
      }
    }
    false
  }

}
