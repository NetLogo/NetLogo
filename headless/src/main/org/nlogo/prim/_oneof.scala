// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, LogoList, Nobody, I18N }
import org.nlogo.nvm.{ Reporter, Context, EngineException, ArgumentTypeException }
import org.nlogo.agent.AgentSet

class _oneof extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType | Syntax.ListType),
      Syntax.WildcardType)

  override def report(context: Context) =
    args(0).report(context) match {
      case agents: AgentSet =>
        report_1(context, agents)
      case list: LogoList =>
        report_2(context, list)
      case obj =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.AgentsetType, obj)
    }

  def report_1(context: Context, agents: AgentSet): AnyRef = {
    val count = agents.count
    if (count == 0)
      Nobody
    else
      agents.randomOne(count, context.job.random.nextInt(count))
  }

  def report_2(context: Context, list: LogoList): AnyRef = {
    val size = list.size
    if (size == 0)
      throw new EngineException(
        context, this,
        I18N.errors.getN("org.nlogo.prim.etc.$common.emptyListInput", displayName))
    list.get(context.job.random.nextInt(size))
  }

  def report_3(context: Context, obj: AnyRef): AnyRef =
    args(0).report(context) match {
      case agents: AgentSet =>
        val count = agents.count
        if (count == 0)
          Nobody
        else
          agents.randomOne(count, context.job.random.nextInt(count))
      case list: LogoList =>
        val size = list.size
        if (size == 0)
          throw new EngineException(
            context, this,
            I18N.errors.getN("org.nlogo.prim.etc.$common.emptyListInput", displayName))
        list.get(context.job.random.nextInt(size))
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.AgentsetType, obj)
    }

}
