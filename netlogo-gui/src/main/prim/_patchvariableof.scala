// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ AgentException, I18N, LogoException, LogoListBuilder, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

class _patchvariableof(private[this] val _vn: Int) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.TurtleType | Syntax.PatchType | Syntax.TurtlesetType | Syntax.PatchsetType),
      Syntax.WildcardType)

  override def toString: String =
    s"${super.toString}:${if (world == null) vn else world.patchesOwnNameAt(vn)}"

  override def report(context: Context): AnyRef = args(0).report(context) match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
        try {
          agent.getPatchVariable(vn)
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
      case sourceSet: AgentSet =>
        val result = new LogoListBuilder
        try {
          val itr = sourceSet.shufflerator(context.job.random)
          while (itr.hasNext)
            result.add(itr.next().getPatchVariable(vn))
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
        result.toLogoList
      case agentOrSet =>
        throw new ArgumentTypeException(context, this, 0,
          Syntax.TurtleType | Syntax.PatchType | Syntax.TurtlesetType | Syntax.PatchsetType,
          agentOrSet)
    }

  def report_1(context: Context, agentOrSet: AnyRef): AnyRef = agentOrSet match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
        try {
          agent.getPatchVariable(_vn)
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
      case sourceSet: AgentSet =>
        val result = new LogoListBuilder
        try {
          val itr = sourceSet.shufflerator(context.job.random)
          while (itr.hasNext)
            result.add(itr.next().getPatchVariable(_vn))
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
        result.toLogoList
      case _ =>
        throw new ArgumentTypeException(context, this, 0,
          Syntax.TurtleType | Syntax.PatchType | Syntax.TurtlesetType | Syntax.PatchsetType,
          agentOrSet)
    }

  def report_2(context: Context, sourceSet: AgentSet): AnyRef = {
    val result = new LogoListBuilder
    try {
      val itr = sourceSet.shufflerator(context.job.random)
      while (itr.hasNext)
        result.add(itr.next().getPatchVariable(_vn))
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }
    result.toLogoList
  }

  def report_3(context: Context, agent: Agent): AnyRef = {
    if (agent.id == -1)
      throw new EngineException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
    try {
      agent.getPatchVariable(_vn)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }
  }

  def vn = _vn
}
