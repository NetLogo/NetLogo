// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.AgentKind
import org.nlogo.agent.{AgentSet, AgentSetBuilder, ArrayAgentSet, Patch, Turtle}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{Context, Reporter}

class _breedhere(breedName: String) extends Reporter {


  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AgentSet = {
    val patch = context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch  => p
    }
    val breed = world.getBreed(breedName)
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle, patch.turtleCount)
    val itr = patch.turtlesHere.iterator
    while (itr.hasNext) {
      val turtle = itr.next()
      if (turtle.getBreed eq breed)
        agentSetBuilder.add(turtle)
    }
    agentSetBuilder.build()
  }
}
