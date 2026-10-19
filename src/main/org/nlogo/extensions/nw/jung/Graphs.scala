// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jung

import java.util.Collection
import org.nlogo.agent.Link
import org.nlogo.agent.Turtle
import org.nlogo.api.ExtensionException
import org.nlogo.extensions.nw.GraphContext
import edu.uci.ics
import edu.uci.ics.jung.graph.util.EdgeType
import edu.uci.ics.jung.graph.util.Pair

import scala.jdk.CollectionConverters.IterableHasAsJava

trait Graph
  extends ics.jung.graph.AbstractGraph[Turtle, Link]
  with Algorithms {

  val gc: GraphContext

  def edgeType =
    if (gc.isDirected)
      ics.jung.graph.util.EdgeType.DIRECTED
    else
      ics.jung.graph.util.EdgeType.UNDIRECTED

  override def getIncidentEdges(turtle: Turtle): Collection[Link] = gc.allEdges(turtle).asJavaCollection

  override def getEdgeCount: Int = gc.linkCount

  override def getNeighbors(turtle: Turtle): Collection[Turtle] = gc.allNeighbors(turtle).asJavaCollection

  override def getVertexCount: Int = gc.turtleCount
  override def getVertices: Collection[Turtle] = gc.nodes.asJavaCollection
  override def getEdges: Collection[Link] = gc.links.asJavaCollection
  override def containsEdge(link: Link): Boolean = gc.outEdges(link.end1).contains(link)
  override def containsVertex(turtle: Turtle): Boolean = gc.nodes.contains(turtle)

  def getEndpoints(link: Link): Pair[Turtle] =
    new Pair(link.end1, link.end2) // Note: contract says nothing about edge being in graph

  def removeEdge(link: Link): Boolean =
    throw sys.error("not implemented")
  def removeVertex(turtle: Turtle): Boolean =
    throw sys.error("not implemented")
  def addVertex(turtle: Turtle): Boolean =
    throw sys.error("not implemented")
  override def addEdge(link: Link, turtles: Collection[? <: Turtle]): Boolean =
    throw sys.error("not implemented")
  def addEdge(link: Link, turtles: Pair[? <: Turtle], edgeType: EdgeType): Boolean =
    throw sys.error("not implemented")
}

class DirectedGraph(
  override val gc: GraphContext)
  extends ics.jung.graph.AbstractTypedGraph[Turtle, Link](EdgeType.DIRECTED)
  with Graph
  with DirectedAlgorithms
  with ics.jung.graph.DirectedGraph[Turtle, Link] {

  if (!gc.isDirected)
    throw new ExtensionException("link set must be directed")

  override def getInEdges(turtle: Turtle): Collection[Link] = gc.inEdges(turtle).asJavaCollection
  override def getPredecessors(turtle: Turtle): Collection[Turtle] = gc.inNeighbors(turtle).asJavaCollection

  override def getOutEdges(turtle: Turtle): Collection[Link] = gc.outEdges(turtle).asJavaCollection
  override def getSuccessors(turtle: Turtle): Collection[Turtle] = gc.outNeighbors(turtle).asJavaCollection

  def isDest(turtle: Turtle, link: Link): Boolean = link.end2 == turtle
  def isSource(turtle: Turtle, link: Link): Boolean = link.end1 == turtle

  override def getSource(link: Link): Turtle = link.end1

  override def getDest(link: Link): Turtle = link.end2

}

class UndirectedGraph(
  override val gc: GraphContext)
  extends ics.jung.graph.AbstractTypedGraph[Turtle, Link](EdgeType.UNDIRECTED)
  with Graph
  with UndirectedAlgorithms
  with ics.jung.graph.UndirectedGraph[Turtle, Link] {

  override def getInEdges(turtle: Turtle) = getIncidentEdges(turtle)
  override def getPredecessors(turtle: Turtle) = getNeighbors(turtle: Turtle)

  override def getOutEdges(turtle: Turtle) = getIncidentEdges(turtle)
  override def getSuccessors(turtle: Turtle) = getNeighbors(turtle: Turtle)

  def isDest(turtle: Turtle, link: Link) = false
  def isSource(turtle: Turtle, link: Link) = false

  override def getSource(link: Link): Turtle = null
  override def getDest(link: Link): Turtle = null

}
