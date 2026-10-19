// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jung

import org.apache.commons.collections15.Factory
import org.nlogo.agent.AgentSet
import org.nlogo.agent.Turtle
import org.nlogo.agent.World
import org.nlogo.extensions.nw.NetworkExtensionUtil.createTurtle
import org.nlogo.api.MersenneTwisterFast

import edu.uci.ics.jung

import scala.jdk.CollectionConverters.CollectionHasAsScala

object DummyGraph {
  // TODO: the vertex id thing is a ugly hack to get around the fact that
  // Jung has no ordered SparseGraph (only ordered MultiGraphs). Ideally,
  // we would add a custom sorted graph. NP 2012-06-13
  private var vertexIdCounter = 0L
  case class Vertex(val id: Long)
  class Edge
  def edgeFactory: Factory[Edge] = new Factory[Edge]() {
    def create = new Edge
  }
  def vertexFactory: Factory[Vertex] = new Factory[Vertex]() {
    def create = {
      vertexIdCounter += 1
      new Vertex(vertexIdCounter)
    }
  }

  def importToNetLogo(
    graph: jung.graph.Graph[Vertex, Edge],
    world: World,
    turtleBreed: AgentSet,
    linkBreed: AgentSet,
    rng: MersenneTwisterFast,
    sorted: Boolean = false) = {

    val vs = graph.getVertices.asScala
    val vertices = if (sorted) vs.toSeq.sortBy(_.id) else vs

    val turtles: Map[Vertex, Turtle] =
      vertices.map { v =>
        v -> createTurtle(world, turtleBreed, rng)
      }.toMap

    graph.getEdges.asScala.foreach { e =>
      createLink(turtles, graph.getEndpoints(e), defaultDirected = false, linkBreed, world)
    }

    turtles.valuesIterator
  }
}
