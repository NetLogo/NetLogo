// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ AgentException, I18N, LogoListBuilder }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

class _breedvariableof(name: String) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.TurtleType | Syntax.TurtlesetType),
      Syntax.WildcardType)

  override def toString: String = s"${super.toString}:$name"

  override def report(context: Context): AnyRef = args(0).report(context) match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
        try {
          agent.getBreedVariable(name)
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
      case sourceSet: AgentSet =>
        val result = new LogoListBuilder
        try {
          val itr = sourceSet.shufflerator(context.job.random)
          while (itr.hasNext)
            result.add(itr.next().getBreedVariable(name))
        } catch {
          case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
        }
        result.toLogoList
      case agentOrSet =>
        throw new ArgumentTypeException(
          context, this, 0,
          Syntax.TurtlesetType | Syntax.TurtleType,
          agentOrSet)
    }
}
