// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim.jung

import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.NetworkExtensionUtil.AgentSetToRichAgentSet
import org.nlogo.extensions.nw.NetworkExtensionUtil.TurtleCreatingCommand
import org.nlogo.extensions.nw.jung.Generator
import org.nlogo.agent

class KleinbergSmallWorldGenerator extends TurtleCreatingCommand {
  override def getSyntax = commandSyntax(
    List(TurtlesetType, LinksetType, NumberType, NumberType, NumberType, BooleanType, CommandBlockType | OptionalType),
    blockAgentClassString = Some("-T--"))
  def createTurtles(args: Array[api.Argument], context: api.Context) = {
    implicit val world = context.world.asInstanceOf[agent.World]
    new Generator(
      turtleBreed = args(0).getAgentSet.requireTurtleBreed,
      linkBreed = args(1).getAgentSet.requireLinkBreed,
      world = world)
      .kleinbergSmallWorld(
        rowCount = getIntValueWithMinimum(args(2), 2, "rows"),
        colCount = getIntValueWithMinimum(args(3), 2, "columns"),
        clusteringExponent = args(4).getDoubleValue,
        isToroidal = args(5).getBooleanValue,
        rng = context.getRNG)
  }
}

class Lattice2DGenerator extends TurtleCreatingCommand {
  override def getSyntax = commandSyntax(
    List(TurtlesetType, LinksetType, NumberType, NumberType, BooleanType, CommandBlockType | OptionalType),
    blockAgentClassString = Some("-T--"))
  def createTurtles(args: Array[api.Argument], context: api.Context) = {
    implicit val world = context.world.asInstanceOf[agent.World]
    new Generator(
      turtleBreed = args(0).getAgentSet.requireTurtleBreed,
      linkBreed = args(1).getAgentSet.requireLinkBreed,
      world = world)
      .lattice2D(
        rowCount = getIntValueWithMinimum(args(2), 2, "rows"),
        colCount = getIntValueWithMinimum(args(3), 2, "columns"),
        isToroidal = args(4).getBooleanValue,
        rng = context.getRNG)
  }
}
