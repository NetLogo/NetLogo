// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim

import org.nlogo.agent
import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.algorithms.InRadius._
import org.nlogo.extensions.nw.GraphContextProvider

trait InRadiusPrim extends api.Reporter {
  val gcp: GraphContextProvider
  val reverse: Boolean
  override def getSyntax = reporterSyntax(
    right = List(NumberType),
    ret = TurtlesetType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context) = {
    val world = context.getAgent.world.asInstanceOf[agent.World]
    val graphContext = gcp.getGraphContext(world)
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val radius = args(0).getIntValue
    if (radius < 0) throw new api.ExtensionException("radius cannot be negative")
    inRadius(graphContext, source, radius, reverse)
  }
}

class TurtlesInRadius(override val gcp: GraphContextProvider)
  extends InRadiusPrim {
  override val reverse = false
}

class TurtlesInReverseRadius(override val gcp: GraphContextProvider)
  extends InRadiusPrim {
  override val reverse = true
}
