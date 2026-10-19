// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim.jgrapht

import org.nlogo.api
import org.nlogo.api.ScalaConversions.toLogoList
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.GraphContextProvider

trait CliquePrim
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = ListType)
  val gcp: GraphContextProvider
  def graph(context: api.Context) = {
    val gc = gcp.getGraphContext(context.getAgent.world)
    if (gc.isDirected)
      throw new api.ExtensionException("Current graph must be undirected")
    gc.asJGraphTGraph
  }
}

class MaximalCliques(override val gcp: GraphContextProvider)
  extends CliquePrim {
  override def report(args: Array[api.Argument], context: api.Context) = {
    toLogoList(
      graph(context)
        .BronKerboschCliqueFinder
        .allCliques(context.getRNG))
  }
}

class BiggestMaximalCliques(override val gcp: GraphContextProvider)
  extends CliquePrim {
  override def report(args: Array[api.Argument], context: api.Context) = {
    toLogoList(
      graph(context)
        .BronKerboschCliqueFinder
        .biggestCliques(context.getRNG))
  }
}
