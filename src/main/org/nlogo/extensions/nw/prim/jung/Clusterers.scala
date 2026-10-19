// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim.jung

import org.nlogo.api
import org.nlogo.api.ScalaConversions.toLogoList
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.GraphContextProvider
import org.nlogo.extensions.nw.util.TurtleSetsConverters.toTurtleSet

class BicomponentClusters(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = ListType)
  override def report(args: Array[api.Argument], context: api.Context) = {
    val graph = gcp.getGraphContext(context.getAgent.world).asUndirectedJungGraph
    toLogoList(graph
      .BicomponentClusterer
      .clusters(context.getRNG))
  }
}

class WeakComponentClusters(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = ListType)
  override def report(args: Array[api.Argument], context: api.Context) = {
    val comps = gcp.getGraphContext(context.getAgent.world).components
    toLogoList(new scala.util.Random(context.getRNG).shuffle(comps.map(toTurtleSet).toSeq))
  }
}
