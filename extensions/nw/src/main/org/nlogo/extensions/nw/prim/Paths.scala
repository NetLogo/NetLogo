// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.prim

import org.nlogo.agent
import org.nlogo.api
import org.nlogo.core.LogoList
import org.nlogo.api.ScalaConversions.toLogoObject
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.NetworkExtensionUtil.{AgentToRichAgent, canonocilizeVar}
import org.nlogo.extensions.nw.GraphContextProvider

class DistanceTo(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType),
    ret = NumberType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.requireAlive.asInstanceOf[agent.Turtle]
    val graphContext = gcp.getGraphContext(context.getAgent.world)
    toLogoObject(graphContext.pathFinder.distance(source, target).getOrElse(false))
  }
}

class WeightedDistanceTo(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType, StringType | SymbolType),
    ret = NumberType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.asInstanceOf[agent.Turtle]
    val weightVariable = canonocilizeVar(args(1).get)
    val distance = gcp.getGraphContext(context.getAgent.world).pathFinder.distance(source, target, Some(weightVariable))
    toLogoObject(distance.getOrElse(false))
  }
}

class PathTo(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType),
    ret = ListType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.requireAlive.asInstanceOf[agent.Turtle]
    val gc = gcp.getGraphContext(context.getAgent.world)
    def turtlesToLinks(turtles: List[agent.Turtle]): Iterator[agent.Link] =
      for {
        (source, target) <- turtles.iterator zip turtles.tail.iterator
        // RNG is necessary because there may be more than one link between the turtles
        links = gc.outEdges(source).filter(l => gc.otherEnd(source)(l) == target)
        l = links(context.getRNG.nextInt(links.size))
      } yield l
    toLogoObject(gc.pathFinder.path(source, target, context.getRNG)
      .map { p => LogoList.fromIterator(turtlesToLinks(p.toList)) }
      .getOrElse(false))
  }
}

class TurtlesOnPathTo(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType),
    ret = ListType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.requireAlive.asInstanceOf[agent.Turtle]
    val graphContext = gcp.getGraphContext(context.getAgent.world)
    toLogoObject(graphContext.pathFinder.path(source, target, context.getRNG)
      .map { p => LogoList.fromIterator(p.iterator) }
      .getOrElse(false))
  }
}

class TurtlesOnWeightedPathTo(gcp:GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType, StringType | SymbolType),
    ret = ListType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.asInstanceOf[agent.Turtle]
    val weightVariable = canonocilizeVar(args(1).get)
    val graphContext = gcp.getGraphContext(context.getAgent.world)
    toLogoObject(graphContext.pathFinder.path(source, target, context.getRNG, Some(weightVariable))
      .map { p => LogoList.fromIterator(p.iterator) }
      .getOrElse(false))
  }
}

class WeightedPathTo(gcp: GraphContextProvider)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(
    right = List(TurtleType, StringType | SymbolType),
    ret = ListType | BooleanType,
    agentClassString = "-T--")
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val source = context.getAgent.asInstanceOf[agent.Turtle]
    val target = args(0).getAgent.asInstanceOf[agent.Turtle]
    val weightVariable = canonocilizeVar(args(1).get)
    val gc = gcp.getGraphContext(context.getAgent.world)
    def turtlesToLinks(turtles: List[agent.Turtle]): Iterator[agent.Link] =
      for {
        (source, target) <- turtles.iterator zip turtles.tail.iterator
        // RNG is necessary because there may be more than one link between the turtles
        links = gc.outEdges(source).filter(l => gc.otherEnd(source)(l) == target)
        l = links(context.getRNG.nextInt(links.size))
      } yield l
    toLogoObject(gc
      .pathFinder.path(source, target, context.getRNG, Some(weightVariable))
      .map { p => LogoList.fromIterator(turtlesToLinks(p.toList)) }
      .getOrElse(false))
  }
}
