// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite
import org.nlogo.api.WorldDimensions3D
import org.nlogo.core.WorldDimensions

class World3DTests extends FunSuite with AbstractTestWorld
{

  val worldSquare = new WorldDimensions3D(-2, 2, -2, 2, -2, 2)
  val worldRectangle = new WorldDimensions3D(-3, 3, -2, 2, -2, 2)
  val turtles5 = Array(Array(0,0,0), Array(0,0,0), Array(0,0,0), Array(0,0,0), Array(0,0,0))
  val turtles2 = Array(Array(0,1,0), Array(0,-1,0))
  val link1 = Array(0, 1)

  override def makeWorld(dimensions: WorldDimensions) = {
    val w = new World3D
    w.createPatches(dimensions)
    w.realloc()
    w
  }
  override def makeTurtle(world: World, cors: Array[Int]) =
    new Turtle3D(world.asInstanceOf[World3D],
                 world.turtles,
                 cors(0).toDouble,
                 cors(1).toDouble,
                 cors(2).toDouble)
  override def makeLink(world: World, ends: Array[Int]) =
    new Link3D(world.asInstanceOf[World3D], world.getTurtle(ends(0)),
               world.getTurtle(ends(1)), world.links)
  test("IteratorSkipsDeadTurtles1_3D") {
    testIteratorSkipsDeadTurtles1(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles2_3D") {
    testIteratorSkipsDeadTurtles2(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles3_3D") {
    testIteratorSkipsDeadTurtles3(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles4_3D") {
    testIteratorSkipsDeadTurtles4(worldSquare, turtles5)
  }
  test("Shufflerator1_3D") {
    testShufflerator1(worldSquare, turtles5)
  }
  test("LinkDistance_3D") {
    testLinkDistance(worldSquare, turtles2, link1)
  }
  test("ShortestPath_3D") {
    testShortestPath(worldRectangle)
  }
  test("ChangePublishedAfterWorldResize_3D") {
    testChangePublishedAfterWorldResize(worldSquare, worldRectangle)
  }
}
