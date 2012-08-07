// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.Assertions
import org.nlogo.api.WorldDimensions

// Exists to be separately extended in 2D and 3D versions. - ST 10/18/10

abstract class AbstractTestWorld extends Assertions {

  def makeWorld(dimensions: WorldDimensions): World
  def makeTurtle(world: World, cors: Array[Int]): Turtle
  def makeLink(world: World, ends: Array[Int]): Link

  ///

  private def makeTurtles(world: World, turtles: Array[Array[Int]]) {
    turtles.foreach(makeTurtle(world, _))
    expect(turtles.size)(world.turtles.count)
  }

  private def checkTurtles(world: World, size: Int) {
    expect(size)(world.turtles.count)
    expect(size)(world.turtles.toLogoList.size)
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
      expect(who)(iter.next.id)
    }
    assert(!iter.hasNext)
  }

  def testLinkDistance(d: WorldDimensions, turtleList: Array[Array[Int]], linkList: Array[Int]) {
    val world = makeWorld(d)
    makeTurtles(world, turtleList)
    val link = makeLink(world, linkList)
    expect(0)(world.protractor.distanceToLink(link, 0, 0))
    expect(1)(world.protractor.distanceToLink(link, 1, 0))
  }

  def testShortestPath(d: WorldDimensions) {
    val world = makeWorld(d)
    expect(3)(world.topology.shortestPathY(2, -2))
    expect(5)(world.topology.shortestPathX(2, -2))
  }
}
