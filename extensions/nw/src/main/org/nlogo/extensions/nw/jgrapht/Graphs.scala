// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jgrapht

import org.nlogo.agent.Link
import org.nlogo.agent.Turtle
import org.nlogo.api
import org.nlogo.api.ExtensionException
import org.nlogo.extensions.nw.GraphContext
import org.nlogo.extensions.nw.util.TurtleSetsConverters.toTurtleSets

import org.jgrapht

import scala.jdk.CollectionConverters.{ SetHasAsJava, CollectionHasAsScala }

trait Graph
  extends jgrapht.graph.AbstractGraph[Turtle, Link] {
  val gc: GraphContext

  override def getAllEdges(sourceVertex: Turtle, targetVertex: Turtle) =
    gc.allEdges(sourceVertex).toSet.filter(_.end2 == targetVertex).asJava

  override def getEdge(sourceVertex: Turtle, targetVertex: Turtle) =
    gc.outEdges(sourceVertex).find(e => gc.otherEnd(sourceVertex)(e) == targetVertex).orNull

  override def containsEdge(sourceVertex: Turtle, targetVertex: Turtle) =
    getEdge(sourceVertex, targetVertex) != null

  override def containsEdge(edge: Link) = gc.outEdges(edge.end1).contains(edge)
  override def containsVertex(vertex: Turtle) = gc.nodes.contains(vertex)

  override def edgeSet() = gc.links.toSet.asJava

  override def edgesOf(vertex: Turtle) = gc.allEdges(vertex).toSet.asJava

  override def vertexSet() = gc.nodes.toSet.asJava

  override def getEdgeSource(edge: Link) = edge.end1
  override def getEdgeTarget(edge: Link) = edge.end2
  override def getEdgeWeight(edge: Link): Double = 1.0 // TODO: figure out how to deal with weigths

  override def getEdgeFactory() = throw sys.error("not implemented")
  override def addEdge(sourceVertex: Turtle, targetVertex: Turtle) = throw sys.error("not implemented")
  override def addEdge(sourceVertex: Turtle, targetVertex: Turtle, edge: Link) = throw sys.error("not implemented")
  override def addVertex(v: Turtle) = throw sys.error("not implemented")
  override def removeAllEdges(edges: java.util.Collection[? <: Link]) = throw sys.error("not implemented")
  override def removeAllEdges(sourceVertex: Turtle, targetVertex: Turtle) = throw sys.error("not implemented")
  override def removeAllVertices(vertices: java.util.Collection[? <: Turtle]) = throw sys.error("not implemented")
  override def removeEdge(sourceVertex: Turtle, targetVertex: Turtle) = throw sys.error("not implemented")
  override def removeEdge(edge: Link) = throw sys.error("not implemented")
  override def removeVertex(vertex: Turtle) = throw sys.error("not implemented")

  object BronKerboschCliqueFinder extends jgrapht.alg.BronKerboschCliqueFinder(this) {
    def allCliques(rng: java.util.Random): Seq[api.AgentSet] =
      toTurtleSets(getAllMaximalCliques.asScala, rng)
    def biggestCliques(rng: java.util.Random): Seq[api.AgentSet] =
      toTurtleSets(getBiggestMaximalCliques.asScala, rng)
  }
}

class UndirectedGraph(
  override val gc: GraphContext)
  extends Graph
  with jgrapht.UndirectedGraph[Turtle, Link] {
  override def degreeOf(vertex: Turtle) = gc.outEdges(vertex).size
}

class DirectedGraph(
  override val gc: GraphContext)
  extends Graph
  with jgrapht.DirectedGraph[Turtle, Link] {
  if (!gc.isDirected)
    throw new ExtensionException("link set must be directed")

  override def incomingEdgesOf(vertex: Turtle) = gc.inEdges(vertex).toSet.asJava
  override def inDegreeOf(vertex: Turtle) = gc.inEdges(vertex).size
  override def outgoingEdgesOf(vertex: Turtle) = gc.outEdges(vertex).toSet.asJava
  override def outDegreeOf(vertex: Turtle) = gc.outEdges(vertex).size

}
