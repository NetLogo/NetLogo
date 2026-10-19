package org.nlogo.extensions.nw.prim

import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.algorithms.MeanPathLength._
import org.nlogo.agent.Turtle
import org.nlogo.extensions.nw.GraphContextProvider
import org.nlogo.extensions.nw.NetworkExtensionUtil.canonocilizeVar

class MeanPathLength(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = NumberType | BooleanType)
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val gc = gcp.getGraphContext(context.getAgent.world)
    val dist = gc.pathFinder.distance(_: Turtle, _: Turtle)
    meanPathLength(gc.nodes, dist)
      .map(Double.box)
      .getOrElse(java.lang.Boolean.FALSE)
  }
}

class MeanWeightedPathLength(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(StringType | SymbolType),
    ret = NumberType | BooleanType)
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val weightVariable = canonocilizeVar(args(0).get)
    val gc = gcp.getGraphContext(context.getAgent.world)
    val dist = gc.pathFinder.distance(_: Turtle, _: Turtle, Some(weightVariable))
    meanPathLength(gc.nodes, dist)
      .map(Double.box)
      .getOrElse(java.lang.Boolean.FALSE)
  }
}
