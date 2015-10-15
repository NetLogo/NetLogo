// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ AgentException, I18N, LogoException, LogoListBuilder, Syntax }
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

class _linkvariableof(private[this] val _vn: Int) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.LinksetType | Syntax.LinkType),
      Syntax.WildcardType)

  override def toString =
      s"${super.toString()}:${if (world != null) world.linksOwnNameAt(vn) else vn}"

  override def report(context: Context): AnyRef = args(0).report(context) match {
    case agent: Agent =>
      if (agent.id == -1)
        throw new EngineException(context, this,
          I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
      try {
        agent.getLinkVariable(_vn)
      } catch {
        case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
      }
    case sourceSet: AgentSet =>
      val result = new LogoListBuilder
      try {
        val itr = sourceSet.shufflerator(context.job.random)
        while (itr.hasNext)
          result.add(itr.next().getLinkVariable(_vn))
      } catch {
        case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
      }
      result.toLogoList
    case agentOrSet =>
      throw new ArgumentTypeException(context, this, 0,
        Syntax.LinksetType | Syntax.LinkType, agentOrSet)
  }

  def report_1(context: Context, agentOrSet: AnyRef): AnyRef = agentOrSet match {
    case agent: Agent =>
      if (agent.id == -1)
        throw new EngineException(context, this,
          I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
      try {
        agent.getLinkVariable(_vn)
      } catch {
        case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
      }
    case sourceSet: AgentSet =>
      val result = new LogoListBuilder
      try {
        val itr = sourceSet.shufflerator(context.job.random)
        while (itr.hasNext)
          result.add(itr.next().getLinkVariable(_vn))
      } catch {
        case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
      }
      result.toLogoList
    case _ =>
      throw new ArgumentTypeException(context, this, 0,
        Syntax.LinksetType | Syntax.LinkType, agentOrSet)
  }

  def report_2(context: Context, agent: Agent): AnyRef = {
    if (agent.id == -1)
      throw new EngineException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
    try {
      agent.getLinkVariable(_vn)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }
  }

  def report_3(context: Context, sourceSet: AgentSet): LogoList = {
    val result = new LogoListBuilder
    try {
      val itr = sourceSet.shufflerator(context.job.random)
      while (itr.hasNext)
        result.add(itr.next().getLinkVariable(_vn))
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }
    result.toLogoList
  }

  def vn = _vn
}
