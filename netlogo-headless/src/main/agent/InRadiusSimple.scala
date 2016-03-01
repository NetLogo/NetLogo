// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  java.util.{ List => JList }

object InRadiusSimple {

  def apply(world: World)(agent: Agent, sourceSet: AgentSet, radius: Double, wrap: Boolean): JList[Agent] = {

    import scala.collection.JavaConverters.{ iterableAsScalaIterableConverter, seqAsJavaListConverter }

    val getAgentXAndY: PartialFunction[Agent, (Double, Double)] = {
      case t: Turtle => (t.xcor,  t.ycor)
      case p: Patch  => (p.pxcor, p.pycor)
    }

    val (originX, originY) = getAgentXAndY(agent)

    val isInRadius = (agent: Agent) => {
      val (xcor, ycor) = getAgentXAndY(agent)
      world.protractor.distance(xcor, ycor, originX, originY, wrap) <= radius
    }

    // `collect` is used in place of `map`ping to `Agent` and `filter`ing, in
    // order to reduce iterations over the collection. --JAB (6/19/14)
    val newAgents = sourceSet.agents.asScala.collect { case agent: Agent if isInRadius(agent) => agent }
    newAgents.toSeq.asJava

  }

}
