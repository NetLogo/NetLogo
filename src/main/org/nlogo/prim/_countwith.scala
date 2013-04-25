// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ Syntax, Dump, I18N }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _countwith extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType, Syntax.BooleanBlockType),
      Syntax.NumberType, "OTPL", "?")

  override def report(context: Context) =
    Double.box(
      report_1(context, argEvalAgentSet(context, 0), args(1)))

  def report_1(context: Context, sourceSet: AgentSet, block: Reporter): Double = {
    block.checkAgentSetClass(sourceSet, context)
    val freshContext = new Context(context, sourceSet)
    var result = 0;
    val iter = sourceSet.iterator
    while(iter.hasNext) {
      val tester = iter.next()
      freshContext.evaluateReporter(tester, block) match {
        case b: java.lang.Boolean =>
          if (b.booleanValue)
            result += 1
        case x =>
          throw new EngineException(
            context, this, I18N.errors.getN(
              "org.nlogo.prim.$common.expectedBooleanValue",
              displayName, Dump.logoObject(tester), Dump.logoObject(x)))
      }
    }
    result
  }

}
