package org.nlogo.extensions.nw.prim

import org.nlogo.api
import org.nlogo.agent
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.GraphContextProvider

class EigenvectorCentrality(gcp: GraphContextProvider) extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = NumberType, agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context) = {
    val gc = gcp.getGraphContext(context.getAgent.world)
    gc.eigenvectorCentrality(context.getAgent.asInstanceOf[agent.Turtle]).asInstanceOf[java.lang.Double]
  }
}
