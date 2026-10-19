package org.nlogo.extensions.nw.algorithms

import org.nlogo.agent
import org.nlogo.agent.{ AgentSet, World, Turtle }
import org.nlogo.api.MersenneTwisterFast
import scala.collection.mutable

object WattsStrogatzGenerator {
  def generate(
    world: World,
    turtleBreed: AgentSet,
    linkBreed: AgentSet,
    nbTurtles: Int,
    neighborhoodSize: Int,
    rewireProbability: Double,
    rng: MersenneTwisterFast): Seq[agent.Turtle] = {

    val turtles = (0 until nbTurtles).map { i =>
      val t = world.createTurtle(turtleBreed)
      t.colorDouble((i % 14) * 10.0 + 5.0)
      t.heading((360.0 * i) / nbTurtles)
      t
    }
    val availBuffer = turtles.toArray

    val adjMap: Map[Turtle, mutable.Set[Turtle]] = turtles.zipWithIndex.map { case (t: Turtle, i: Int) =>
      val targets: mutable.Set[Turtle] = (1 to neighborhoodSize).map(j => turtles((i + j) % nbTurtles)).to(mutable.Set)
        t -> targets
    }.toMap

    for {
      (source, i) <- turtles.zipWithIndex
      neighbor <- 1 to neighborhoodSize
    } {
      val target = turtles((i + neighbor) % nbTurtles)
      val realTarget = if (rng.nextDouble < rewireProbability) {
        adjMap(source).remove(target)

        val newTarget = Iterator.from(0).map { i =>
          val j = rng.nextInt(availBuffer.size - i) + i
          val t = availBuffer(j)
          availBuffer(j) = availBuffer(i)
          availBuffer(i) = t
          t
        }.dropWhile {
          t => t == source || adjMap(source).contains(t) || adjMap(t).contains(source)
        }.next()

        adjMap(source).add(newTarget)
        newTarget
      } else target
      world.linkManager.createLink(source, realTarget, linkBreed)
    }
    turtles
  }
}
