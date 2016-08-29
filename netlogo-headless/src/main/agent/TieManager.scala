// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.agent

import
  scala.util.control.Exception.ignoring

import
  org.nlogo.api.{ AgentException, Numbers },
    Numbers.Infinitesimal

class TieManager(private val world: World) {

  private var tieCount = 0

  def hasTies: Boolean = tieCount > 0

  def reset(): Unit = {
    tieCount = 0
  }

  def setTieMode(link: Link, mode: String): Unit = {
    if (link.isTied && mode == Link.MODE_NONE)
      tieCount -= 1
    else if (mode != Link.MODE_NONE)
      tieCount += 1
  }

  protected def tiedTurtles(root: Turtle, seenTurtles: Set[Turtle]): Seq[Turtle] = {
    val linksFrom = world.linkManager.findLinksFrom(root, world.links)
    val linksTo   = world.linkManager.findLinksTo  (root, world.links)
    val tiedFromTurtles = linksFrom.collect { case link if link.isTied => link.end2 }
    val tiedToTurtles   = linksTo  .collect { case link if !link.getBreed.isDirected && link.isTied => link.end1 }
    (tiedFromTurtles ++ tiedToTurtles).filterNot(t => t.id == -1 || seenTurtles(t)).toSeq.distinct
  }

  private[agent] def turtleMoved(root: Turtle, newX: Double, newY: Double, oldX: Double, oldY: Double): Unit =
    turtleMoved(root, newX, newY, oldX, oldY, Set(root))

  private[agent] def turtleMoved(root: Turtle, newX: Double, newY: Double, oldX: Double, oldY: Double, seenTurtles: Set[Turtle]): Unit = {
    val turtles        = tiedTurtles(root, seenTurtles)
    val allSeenTurtles = seenTurtles ++ turtles
    turtles foreach {
      t =>
        val dx = newX - oldX
        val dy = newY - oldY
        ignoring(classOf[AgentException]) {
          t.xandycor(t.xcor + dx, t.ycor + dy, allSeenTurtles)
        }
    }
  }

  private[agent] def turtleTurned(root: Turtle, newHeading: Double, oldHeading: Double): Unit =
    turtleTurned(root, newHeading, oldHeading, Set(root))

  private[agent] def turtleTurned(root: Turtle, newHeading: Double, oldHeading: Double, seenTurtles: Set[Turtle]) {

    val isLinkedFixedly = (t: Turtle) => world.linkManager.findLink(root, t, world.links, true).mode == Link.MODE_FIXED
    val getCoords       = (t: Turtle) => (t.xcor(), t.ycor())
    val squash          = (x: Double) => if (StrictMath.abs(x) < Infinitesimal) 0 else x
    val squashedSin     = StrictMath.toRadians _ andThen StrictMath.sin andThen squash
    val squashedCos     = StrictMath.toRadians _ andThen StrictMath.cos andThen squash

    val dh             = Turtle.subtractHeadings(newHeading, oldHeading)
    val (x, y)         = getCoords(root)
    val turtles        = tiedTurtles(root, seenTurtles)
    val allSeenTurtles = seenTurtles ++ turtles

    turtles foreach {
      t =>

        val wentBoom =
          try {
            val r = world.protractor.distance(root, t, true)
            if (r != 0) {
              val theta = world.protractor.towards(root, t, true) + dh
              val newX2 = x + r * squashedSin(theta)
              val newY2 = y + r * squashedCos(theta)
              t.xandycor(newX2, newY2, allSeenTurtles)
            }
            false
          }
          catch {
            case _: AgentException => true
          }

        if (isLinkedFixedly(t) && !wentBoom)
          t.heading(t.heading + dh, allSeenTurtles)

    }

  }

}
