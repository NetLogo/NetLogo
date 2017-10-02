// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.util.ArrayList

import org.scalatest.FunSuite

class GetRegionTests extends FunSuite {

  def toArrayList(l: List[(Int, Int)]): ArrayList[(Int, Int)] = {
    val res = new ArrayList[(Int, Int)]
    l.foreach {
      res.add
    }
    res
  }

  def testGetRegion(x: Int, y: Int, r: Int, w: Int, h: Int, xWraps: Boolean, yWraps: Boolean) = {
    val world = new World2D
    world.createPatches(0, w - 1, 0, h - 1)

    assert(world.worldWidth == w)
    assert(world.worldHeight == h)
    world.changeTopology(xWraps, yWraps)
    world.topology.getRegion(x, y, r)
  }

  // ticket #1038
  test("getRegion no wrap") {
    assertResult(toArrayList(List((1, 3), (4, 6), (7, 9))))(testGetRegion(2, 1, 1, 3, 3, false, false))
    assertResult(toArrayList(List((0, 9))))(testGetRegion(2, 1, 2, 3, 3, false, false))
    assertResult(toArrayList(List(
      (2, 6), (8, 12), (14, 18), (20, 24), (26, 30)
    )))(testGetRegion(4, 3, 2, 6, 6, false, false))

    assertResult(toArrayList(List((1, 3), (4, 6))))(testGetRegion(2, 0, 1, 3, 2, false, false))

    assertResult(toArrayList(List(
      (3, 7)
    )))(testGetRegion(5, 0, 2, 7, 1, false, false))
  }

  test("testGetRegion with x wrap") {
    assertResult(toArrayList(List(
      (0, 1), (2, 7), (8, 13), (14, 19), (20, 25), (26, 30)
    )))(testGetRegion(4, 3, 2, 6, 6, true, false))

    assertResult(toArrayList(List(
      (0, 1), (3, 7)
    )))(testGetRegion(5, 0, 2, 7, 1, true, false))

    assertResult(toArrayList(List(
      (3, 7)
    )))(testGetRegion(0, 1, 2, 1, 7, true, false))

    assertResult(toArrayList(List(
      (0, 4), (6, 7)
    )))(testGetRegion(1, 0, 2, 7, 1, true, false))
  }

  test("getRegion with y wrap") {
    assertResult(toArrayList(List(
      (0, 1), (3, 7)
    )))(testGetRegion(0, 1, 2, 1, 7, false, true))

    assertResult(toArrayList(List(
      (0, 4), (6, 7)
    )))(testGetRegion(0, 5, 2, 1, 7, false, true))

    assertResult(toArrayList(List(
      (0, 4)
    )))(testGetRegion(1, 0, 2, 7, 1, false, true))
  }

  test("getRegion with x&y wrap") {
    assertResult(toArrayList(List((0, 9))))(testGetRegion(2, 1, 1, 3, 3, true, true))
    assertResult(toArrayList(List((0, 9))))(testGetRegion(2, 1, 2, 3, 3, true, true))

    assertResult(toArrayList(List(
      (0, 1), (2, 7), (8, 13), (14, 19), (20, 25), (26, 30)
    )))(testGetRegion(4, 3, 2, 6, 6, true, true))

    assertResult(toArrayList(List((0, 6))))(testGetRegion(2, 0, 1, 3, 2, true, true))
  }
}
