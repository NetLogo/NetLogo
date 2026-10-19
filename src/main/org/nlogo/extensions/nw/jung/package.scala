package org.nlogo.extensions.nw

import org.apache.commons.collections15.Factory
import org.apache.commons.collections15.Transformer
import org.nlogo.agent.{AgentSet, Directedness, Turtle, World}
import edu.uci.ics.jung.{graph => jg}
import edu.uci.ics.jung.graph.util.Pair

package object jung {

  def transformer[A, B](f: A => B) =
    new Transformer[A, B]() {
      override def transform(a: A): B = f(a)
    }

  def factory[A](f: => A) =
    new Factory[A]() {
      override def create = f
    }

  def factoryFor[V, E](linkBreed: AgentSet): Factory[jg.Graph[V, E]] =
    if (linkBreed.isDirected)
      factory { new jg.DirectedSparseGraph[V, E] }
    else
      factory { new jg.UndirectedSparseGraph[V, E] }

  def directedFactory[V, E] = jg.DirectedSparseGraph.getFactory[V, E]
  def undirectedFactory[V, E] = jg.UndirectedSparseGraph.getFactory[V, E]
  def sparseGraphFactory[V, E] = jg.SparseMultigraph.getFactory[V, E]

  def createLink[V](turtles: Map[V, Turtle], endPoints: Pair[V], defaultDirected: Boolean, linkBreed: AgentSet, world: World) = {
    if (linkBreed.directed == Directedness.Undetermined) {
      linkBreed.setDirected(defaultDirected)
    }
    world.linkManager.createLink(
      turtles(endPoints.getFirst),
      turtles(endPoints.getSecond),
      linkBreed)
  }

}
