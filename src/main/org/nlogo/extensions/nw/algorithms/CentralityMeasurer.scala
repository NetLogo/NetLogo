package org.nlogo.extensions.nw.algorithms

import org.nlogo.agent.Turtle

trait CentralityMeasurer {
  def inNeighbors(turtle: Turtle): Iterable[Turtle]
  def components: Iterable[Iterable[Turtle]]

  // Initializing with in-degree works well with directed graphs, knocking out obviously non-strongly reachable nodes
  // immediately. Initializing with all ones can make convergence take much longer. -- BCH 5/12/2014
  private def inDegrees(turtles: Iterable[Turtle]) =
    turtles.foldLeft(Map.empty[Turtle, Double]) {
      (m, t) => m + (t -> inNeighbors(t).size.toDouble)
    }

  lazy val eigenvectorCentrality: Map[Turtle, Double] = components.flatMap { turtles =>
    Iterator.iterate(inDegrees(turtles))((last) => {
      val result = last map {
        // Leaving the last score allows us to handle networks for which power iteration normally fails, e.g. 0--1--2
        // Gephi does this -- BCH 5/12/2014
        case (turtle: Turtle, lastScore: Double) =>
          turtle -> (lastScore + (inNeighbors(turtle) map last).sum)
      }
      // This is how gephi normalizes -- BCH 5/12/2014
      val normalizer = result.values.max
      if (normalizer > 0) {
        result map {
          case (turtle: Turtle, score: Double) => turtle -> score / normalizer
        }
      } else {
        // Everything is disconnected... just give everyone 1s -- BCH 5/12/2014
        result map {
          case (turtle: Turtle, score: Double) => turtle -> 1.0
        }
      }
    }).drop(100).next()
  }.toMap

}
