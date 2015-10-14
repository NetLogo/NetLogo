// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.Assertions
import org.nlogo.api.WorldDimensions

// Exists to be separately extended in 2D and 3D versions. - ST 10/18/10

trait AbstractTestWorld extends Assertions {

  def makeWorld(dimensions: WorldDimensions): World
  def makeTurtle(world: World, cors: Array[Int]): Turtle
  def makeLink(world: World, ends: Array[Int]): Link

  ///

  private def makeTurtles(world: World, turtles: Array[Array[Int]]) {
    turtles.foreach(makeTurtle(world, _))
    assertResult(turtles.size)(world.turtles.count)
  }

  private def checkTurtles(world: World, size: Int) {
    assertResult(size)(world.turtles.count)
    assertResult(size)(world.turtles.toLogoList.size)
    assert(world.turtles.toLogoList.forall(_ != null))
  }

  def testIteratorSkipsDeadTurtles1(d: WorldDimensions, turtleList: Array[Array[Int]]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    world.getTurtle(2).die()
    checkTurtles(world, 4)
  }

  def testIteratorSkipsDeadTurtles2(d: WorldDimensions, turtleList: Array[Array[Int]]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    world.getTurtle(0).die()
    checkTurtles(world, 4)
  }

  def testIteratorSkipsDeadTurtles3(d: WorldDimensions, turtleList: Array[Array[Int]]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    world.getTurtle(4).die()
    checkTurtles(world, 4)
  }

  def testIteratorSkipsDeadTurtles4(d: WorldDimensions, turtleList: Array[Array[Int]]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    world.getTurtle(3).die()
    world.getTurtle(4).die()
    checkTurtles(world, 3)
  }

  ///

  def testShufflerator1(d: WorldDimensions, turtleList: Array[Array[Int]]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    world.mainRNG.setSeed(26394)
    val iter = world.turtles.shufflerator(world.mainRNG)
    for(who <- Seq(4, 3, 2, 0, 1)) {
      assert(iter.hasNext)
      assertResult(who)(iter.next.id)
    }
    assert(!iter.hasNext)
  }

  def testLinkDistance(d: WorldDimensions, turtleList: Array[Array[Int]], linkList: Array[Int]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    val link = makeLink(world, linkList)
    assertResult(0)(world.protractor.distanceToLink(link, 0, 0))
    assertResult(1)(world.protractor.distanceToLink(link, 1, 0))
  }

  def testShortestPath(d: WorldDimensions) {
    val world = makeWorld(d)
    assertResult(3)(world.topology.shortestPathY(2, -2))
    assertResult(5)(world.topology.shortestPathX(2, -2))
  }

  def testChangePublishedAfterWorldResize(d1: WorldDimensions, d2: WorldDimensions) {
    val world = makeWorld(d1)
    val turtleSub =
      new SimpleChangeEventCounter(world.turtles.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher)
    val linkSub =
      new SimpleChangeEventCounter(world.links.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher)
    assertResult(0)(turtleSub.eventCount)
    assertResult(0)(linkSub.eventCount)
    world.createPatches(d2)
    assertResult(1)(turtleSub.eventCount)
    assertResult(1)(linkSub.eventCount)
  }
}
