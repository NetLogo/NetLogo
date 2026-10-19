// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim.jung

import org.nlogo.api
import org.nlogo.agent.World
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.GraphContextProvider
import org.nlogo.extensions.nw.NetworkExtensionUtil.AgentSetToRichAgentSet
import org.nlogo.extensions.nw.NetworkExtensionUtil.TurtleCreatingCommand
import org.nlogo.extensions.nw.jung.io.Matrix

class SaveMatrix(gcp: GraphContextProvider)
  extends api.Command {
  override def getSyntax = commandSyntax(List(StringType))
  override def perform(args: Array[api.Argument], context: api.Context): Unit = {
    val graph = gcp.getGraphContext(context.getAgent.world).asJungGraph
    val fm = context.asInstanceOf[org.nlogo.nvm.ExtensionContext].workspace.fileManager
    Matrix.save(graph, fm.attachPrefix(args(0).getString))
  }
}

class LoadMatrix
  extends TurtleCreatingCommand {
  override def getSyntax = commandSyntax(List(StringType, TurtlesetType, LinksetType, CommandBlockType | OptionalType), blockAgentClassString = Some("-T--"))
  def createTurtles(args: Array[api.Argument], context: api.Context) = {
    implicit val world = context.world.asInstanceOf[World]
    val fm = context.asInstanceOf[org.nlogo.nvm.ExtensionContext].workspace.fileManager
    Matrix.load(
      filename = fm.attachPrefix(args(0).getString),
      turtleBreed = args(1).getAgentSet.requireTurtleBreed,
      linkBreed = args(2).getAgentSet.requireLinkBreed,
      world = world,
      rng = context.getRNG)
  }
}
