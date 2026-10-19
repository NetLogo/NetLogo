// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jung

import org.nlogo.agent.{World, Agent, Link, Turtle}
import org.nlogo.api.ExtensionException
import org.nlogo.extensions.nw.NetworkExtensionUtil.LinkToRichLink
import org.nlogo.extensions.nw.NetworkExtensionUtil.functionToTransformer
import org.nlogo.extensions.nw.util.TurtleSetsConverters.toTurtleSets

import edu.uci.ics.jung.{ algorithms => jungalg }
import org.nlogo.agent.World.VariableWatcher
import org.nlogo.extensions.nw.util.CacheManager

import scala.collection.mutable
import scala.jdk.CollectionConverters.SetHasAsScala

trait Algorithms {
  self: Graph =>

  val weightedGraphCaches: mutable.Map[String, jungalg.shortestpath.DijkstraShortestPath[Turtle, Link]] = new mutable.HashMap[String, jungalg.shortestpath.DijkstraShortestPath[Turtle, Link]]()

  val cacheInvalidator: World.VariableWatcher = new VariableWatcher {
    def update(agent: Agent, variable: String, value: AnyRef) = agent match {
      case link: Link => if (gc.outEdges(link.end1) contains link) {
        weightedGraphCaches.remove(variable)
        gc.world.deleteWatcher(variable, cacheInvalidator)
      }
      case _ =>
    }
  }

  def weightedDijkstraShortestPath(variable: String) = {
    getOrCreateCache(variable)
  }

  def getOrCreateCache(variable: String) = {
    weightedGraphCaches.getOrElseUpdate(variable, {
      val weightFunction = (link: Link) => {
        implicit val world = gc.world
        val value = link.getBreedOrLinkVariable(variable)
        try value.asInstanceOf[java.lang.Number]
        catch {
          case e: Exception => throw new ExtensionException("Weight variable must be numeric.")
        }
      }
      gc.world.addWatcher(variable, cacheInvalidator)
      new jungalg.shortestpath.DijkstraShortestPath(self, weightFunction, true)
    })
  }

  def betweennessCentrality(agent: Agent) = betweennessCentralityCache()(agent)
  def betweennessCentrality(agent: Agent, weightVar: String) = betweennessCentralityCache(Some(weightVar))(agent)

  val betweennessCentralityCache = CacheManager[Agent, Double](gc.world, {
    case None => new UnweightedBetweennessCentrality().get
    case Some(varName: String) => new WeightedBetweennessCentrality(varName).get
  }: Option[String] => Agent => Double)

  class WeightedBetweennessCentrality(variable: String)
    extends jungalg.scoring.BetweennessCentrality(self, gc.weightFunction(variable).andThen(_.asInstanceOf[java.lang.Double]))
    with BetweennessCentrality

  class UnweightedBetweennessCentrality
    extends jungalg.scoring.BetweennessCentrality(self)
    with BetweennessCentrality

  trait BetweennessCentrality extends jungalg.scoring.BetweennessCentrality[Turtle, Link] {
    def get(agent: Agent) = agent match {
      case (t: Turtle) => getVertexScore(t)
      case (l: Link) => getEdgeScore(l)
      case _ => throw new IllegalStateException
    }
  }

  object PageRank extends jungalg.scoring.PageRank(this, 0.15) {
    evaluate()
    def getScore(turtle: Turtle) = {
      if (!graph.containsVertex(turtle))
        throw new ExtensionException(s"$turtle is not a member of the current graph context.")
      getVertexScore(turtle)
    }
  }

  def closenessCentrality(turtle: Turtle) = closenessCentralityCache()(turtle)
  def closenessCentrality(turtle: Turtle, weightVar: String) = closenessCentralityCache(Some(weightVar))(turtle)

  val closenessCentralityCache = CacheManager[Turtle, Double](gc.world,{
    case None => new UnweightedClosenessCentrality().getScore
    case Some(varName: String) => new WeightedClosenessCentrality(varName).getScore
  }: Option[String] => Turtle => Double)

  class UnweightedClosenessCentrality
    extends jungalg.scoring.ClosenessCentrality(this)
    with ClosenessCentrality

  class WeightedClosenessCentrality(variable: String)
    extends jungalg.scoring.ClosenessCentrality(this, gc.weightFunction(variable).andThen(_.asInstanceOf[java.lang.Double]))
    with ClosenessCentrality

  trait ClosenessCentrality extends jungalg.scoring.ClosenessCentrality[Turtle, Link] {
    def getScore(turtle: Turtle) = {
      if (!self.containsVertex(turtle))
        throw new ExtensionException(s"$turtle is not a member of the current graph context.")
      val res = getVertexScore(turtle)
      if (res.isNaN)
        Double.box(0.0) // for isolates
      else res
    }
  }
}

trait UndirectedAlgorithms extends Algorithms {
  self: UndirectedGraph =>
  object BicomponentClusterer
    extends jungalg.cluster.BicomponentClusterer[Turtle, Link] {
    def clusters(rng: java.util.Random) = toTurtleSets(transform(self).asScala, rng)
  }
}

trait DirectedAlgorithms extends Algorithms {
  self: DirectedGraph =>
}
