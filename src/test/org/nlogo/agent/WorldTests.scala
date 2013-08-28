// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite
import org.nlogo.api.WorldDimensions

class WorldTests extends FunSuite with AbstractTestWorld {

  val worldSquare = new WorldDimensions(-2, 2, -2, 2)
  val worldRectangle = new WorldDimensions(-3, 3, -2, 2)
  val turtles5 = Array(Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0))
  val turtles2 = Array(Array(0, 1), Array(0, -1))
  val link1 = Array(0, 1)

  override def makeWorld(dimensions: WorldDimensions) =
    new World() {
      createPatches(dimensions)
      realloc()
    }
  override def makeTurtle(world: World, cors: Array[Int]) =
    new Turtle(world, world.turtles(),
               cors(0).toDouble, cors(1).toDouble)
  override def makeLink(world: World, ends: Array[Int]) =
    new Link(world, world.getTurtle(ends(0)),
             world.getTurtle(ends(1)), world.links)
  test("IteratorSkipsDeadTurtles1") {
    testIteratorSkipsDeadTurtles1(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles2") {
    testIteratorSkipsDeadTurtles2(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles3") {
    testIteratorSkipsDeadTurtles3(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles4") {
    testIteratorSkipsDeadTurtles4(worldSquare, turtles5)
  }
  test("Shufflerator1") {
    testShufflerator1(worldSquare, turtles5)
  }
  test("LinkDistance") {
    testLinkDistance(worldSquare, turtles2, link1)
  }
  test("ShortestPath") {
    testShortestPath(worldRectangle)
  }
  test("ShortestPathHorizontalCylinder") {
    val world = makeWorld(worldRectangle)
    world.changeTopology(false, true)
    assertResult(3.0)(world.topology.shortestPathY(2, -2))
    assertResult(-2.0)(world.topology.shortestPathX(2, -2))
  }
  test("ChangePublishedAfterWorldResize") {
    testChangePublishedAfterWorldResize(worldSquare, worldRectangle)
  }
}
