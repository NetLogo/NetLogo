// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim

import org.nlogo.agent.AgentSet
import org.nlogo.{ api, agent }
import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.core.Syntax._
import org.nlogo.core.LogoList
import org.nlogo.extensions.nw.GraphContext
import org.nlogo.extensions.nw.GraphContextManager
import org.nlogo.extensions.nw.GraphContextProvider
import org.nlogo.extensions.nw.NetworkExtensionUtil.AgentSetToRichAgentSet
import org.nlogo.nvm.AssemblerAssistant
import org.nlogo.nvm.CustomAssembled
import org.nlogo.nvm.ExtensionContext

class SetContext(gcm: GraphContextManager)
  extends api.Command {
  override def getSyntax = commandSyntax(
    right = List(AgentsetType, AgentsetType))
  override def perform(args: Array[api.Argument], context: api.Context): Unit = {
    implicit val world = context.world.asInstanceOf[agent.World]
    val turtleSet = args(0).getAgentSet.requireTurtleSet
    val linkSet = args(1).getAgentSet.requireLinkSet
    val gc = new GraphContext(world, turtleSet, linkSet)
    gcm.setGraphContext(gc)
  }
}

class GetContext(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = ListType)
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val gc = gcp.getGraphContext(context.world.asInstanceOf[org.nlogo.agent.World])
    LogoList(gc.turtleSet, gc.linkSet)
  }
}

class WithContext(gcp: GraphContextProvider)
  extends api.Command
  with CustomAssembled {
  override def getSyntax = commandSyntax(
    right = List(AgentsetType, AgentsetType, CommandBlockType))

  def perform(args: Array[Argument], context: Context): Unit = {
    implicit val world = context.world.asInstanceOf[agent.World]
    val turtleSet = args(0).getAgentSet.requireTurtleSet
    val linkSet = args(1).getAgentSet.requireLinkSet
    val gc = new GraphContext(world, turtleSet, linkSet)
    val extContext = context.asInstanceOf[ExtensionContext]
    val nvmContext = extContext.nvmContext
    // Note that this can optimized by hanging onto the array and just mutating it. Shouldn't be necessary though.
    val agentSet = AgentSet.fromAgent(nvmContext.agent)
    gcp.withTempGraphContext(gc) { () =>
      nvmContext.runExclusiveJob(agentSet, nvmContext.ip + 1)
    }
  }

  def assemble(a: AssemblerAssistant): Unit = {
    a.block()
    a.done()
  }
}
