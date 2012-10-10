// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, I18N, AgentException, LogoList, LogoListBuilder }
import org.nlogo.nvm.{ Reporter, Context, EngineException, ArgumentTypeException }
import org.nlogo.agent.{ Agent, AgentSet }

class _turtleorlinkvariableof(_varName: String) extends Reporter {

  override def toString =
    super.toString + ":" + varName

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.LinkType | Syntax.LinksetType | Syntax.TurtleType | Syntax.TurtlesetType),
      Syntax.WildcardType)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep varName and _varName separate - ST 9/22/12
  def varName = _varName

  override def report(context: Context) =
    report_1(context, args(0).report(context))

  def report_1(context: Context, agentOrSet: AnyRef): AnyRef =
    agentOrSet match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead",
                             agent.classDisplayName))
        try agent.getTurtleOrLinkVariable(_varName)
        catch { case ex: AgentException =>
          throw new EngineException(context, this, ex.getMessage) }
      case sourceSet: AgentSet =>
        val result = new LogoListBuilder
        try {
          val iter = sourceSet.shufflerator(context.job.random)
          while(iter.hasNext)
            result.add(iter.next().getTurtleOrLinkVariable(_varName))
        } catch { case ex: AgentException =>
          throw new EngineException(context, this, ex.getMessage) }
        result.toLogoList
     case _ =>
      throw new ArgumentTypeException(
        context, this, 0,
        Syntax.TurtlesetType | Syntax.TurtleType,
        agentOrSet)
    }

  def report_2(context: Context, agent: Agent): AnyRef = {
    if (agent.id == -1)
      throw new EngineException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead",
                         agent.classDisplayName))
    try agent.getTurtleOrLinkVariable(_varName)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
  }

  def report_3(context: Context, sourceSet: AgentSet): LogoList = {
    val result = new LogoListBuilder
    try {
      val iter = sourceSet.shufflerator(context.job.random)
      while(iter.hasNext)
        result.add(iter.next().getTurtleOrLinkVariable(_varName))
    } catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
    result.toLogoList
  }

}
