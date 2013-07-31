// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, I18N, Dump }
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.AgentSet

class _anyotherwith extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType, Syntax.BooleanBlockType),
      Syntax.BooleanType, "-TPL",
      "?")  // takes reporter block of unknown agent type

  override def report(context: Context) =
    Boolean.box(report_1(context, argEvalAgentSet(context, 0), args(1)))

  def report_1(context: Context, sourceSet: AgentSet, reporterBlock: Reporter): Boolean = {
    val freshContext = new Context(context, sourceSet)
    reporterBlock.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.iterator
    while(iter.hasNext) {
      val tester = iter.next()
      if (tester ne context.agent)
        freshContext.evaluateReporter(tester, reporterBlock) match {
          case b: java.lang.Boolean =>
            if (b.booleanValue)
              return true
          case x =>
            throw new EngineException(
              context, this, I18N.errors.getN(
                "org.nlogo.prim.$common.expectedBooleanValue",
                displayName, Dump.logoObject(tester), Dump.logoObject(x)))
        }
    }
    false
  }

}
