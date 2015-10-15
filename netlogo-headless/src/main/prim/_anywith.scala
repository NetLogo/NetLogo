// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, EngineException, Reporter }

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
          throw new EngineException(
            context, this, I18N.errors.getN(
              "org.nlogo.prim.$common.withExpectedBooleanValue",
              Dump.logoObject(tester), Dump.logoObject(x)))
      }
    }
    false
  }

}
