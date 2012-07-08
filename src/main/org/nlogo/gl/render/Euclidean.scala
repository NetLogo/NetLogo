// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.util.Comparator
import org.nlogo.api.{ Agent, Observer, Turtle, Turtle3D, Patch, Patch3D, Link, Link3D }

/**
 * Sorts agents based on their distance from the observer.
 */
private class Euclidean(observer: Observer) extends Comparator[Agent] {

  override def compare(a1: Agent, a2: Agent): Int =
    distance(a2) compareTo distance(a1)

 /** Computes the Euclidean Squared distance between the given renderable object
  * and the observer. This is like ordinary distance, but without the square root.
  * We're using it for two reasons:
  *     1. It ought to be quicker to compute than ordinary Euclidean distance, which
  *        is important if we're performing this operation for every renderable object
  *        in the scene each frame.
  *     2. It turns out that using ordinary Euclidean distance results in much more
  *        flickering. I'm not sure why this is.
  */
  private def distance(agent: Agent): Double = {
    val (x, y, z) = agent match {
      case t: Turtle3D =>
        (t.xcor, t.ycor, t.zcor)
      case t: Turtle =>
        (t.xcor, t.ycor, 0d)
      case p: Patch3D =>
        (p.pxcor.toDouble, p.pycor.toDouble, p.pzcor.toDouble)
      case p: Patch =>
        (p.pxcor.toDouble, p.pycor.toDouble, 0d)
      case l: Link3D =>
        ((l.x1 + l.x2) / 2, (l.y1 + l.y2) / 2, (l.z1 + l.z2) / 2)
      case l: Link =>
        ((l.x1 + l.x2) / 2, (l.y1 + l.y2) / 2, 0d)
    }
    val (oxcor, oycor, ozcor) =
      (observer.oxcor, observer.oycor, observer.ozcor)
    ((x - oxcor) * (x - oxcor) +
     (y - oycor) * (y - oycor) +
     (z - ozcor) * (z - ozcor))
  }

}
