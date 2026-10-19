// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.algorithms

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.nlogo.agent.Turtle

object MeanPathLength {
  def meanPathLength(
    turtles: Iterable[Turtle],
    distance: (Turtle, Turtle) => Option[Double]): Option[Double] = {
    var sum = 0.0
    var n = 0
    breakable {
      for {
        source <- turtles
        target <- turtles
        if target != source
        dist = distance(source, target)
      } {
        if (dist.isEmpty) {
          sum = Double.NaN
          break()
        }
        n += 1
        sum += dist.get
      }
    }
    Option(sum / n).filterNot(_.isNaN)
  }
}
