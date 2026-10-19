package org.nlogo.extensions.nw.algorithms

import org.nlogo.agent.{AgentSet, Link, Turtle, World}
import org.nlogo.api.MersenneTwisterFast
import org.nlogo.extensions.nw.NetworkExtensionUtil.createTurtle

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object BarabasiAlbertGenerator {
  def generate( world: World,
                turtleBreed: AgentSet,
                linkBreed: AgentSet,
                numTurtles: Int,
                minDegree: Int,
                rng: MersenneTwisterFast): Seq[Turtle] = {
    require(numTurtles > minDegree, "The number of turtles must be larger than the minimum degree.")

    val turtles = ArrayBuffer.fill(minDegree + 1)(createTurtle(world, turtleBreed, rng))
    val links = turtles.combinations(2).map {
      case s +: t +: tail => world.getOrCreateLink(s, t, linkBreed)
      case _ => throw new IllegalStateException
    }.to(ArrayBuffer)

    for (_ <- turtles.size until numTurtles) {
      val s = createTurtle(world, turtleBreed, rng)
      val ls = mutable.LinkedHashSet[Link]()
      while (ls.size < minDegree) {
        // Grabbing a random end of a random link is a very fast and simple way of sampling on degree. However, it is
        // not generalizable to different weighting schemes.
        val l = links(rng.nextInt(links.length))
        val t = if (rng.nextBoolean) l.end1 else l.end2
        ls.add(world.getOrCreateLink(s, t, linkBreed))
      }
      links ++= ls
      turtles += s
    }

    turtles.toSeq
  }
}
