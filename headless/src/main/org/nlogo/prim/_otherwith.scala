// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Dump, I18N }
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.{ Agent, AgentSet, ArrayAgentSet }

class _otherwith extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType, Syntax.BooleanBlockType),
      Syntax.AgentsetType,
      "OTPL", "?")

  override def report(context: Context) =
    report_1(context, argEvalAgentSet(context, 0), args(1))

  def report_1(context: Context, sourceSet: AgentSet, reporterBlock: Reporter): AgentSet = {
    val freshContext = new Context(context, sourceSet)
    val result = collection.mutable.ArrayBuffer[Agent]()
    reporterBlock.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.iterator
    while(iter.hasNext) {
      val tester = iter.next()
      if (tester ne context.agent)
        freshContext.evaluateReporter(tester, reporterBlock) match {
          case b: java.lang.Boolean =>
            if (b.booleanValue)
              result += tester
          case x =>
            throw new EngineException(
              context, this, I18N.errors.getN(
                "org.nlogo.prim.$common.expectedBooleanValue",
                displayName, Dump.logoObject(tester), Dump.logoObject(x)))
        }
    }
    new ArrayAgentSet(sourceSet.kind, result.toArray, world)
  }

}
