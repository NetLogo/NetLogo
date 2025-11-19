// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.util.AnyFunSuiteEx

import Topology.wrapPcor

class TopologyTests extends AnyFunSuiteEx {

  def wrap(pos: Double, min: Double, max: Double): Double =
    Topology.wrap(pos, min, max)
      .ensuring(result => result >= min && result < max)

  test("wrap1") {
    // in the middle of range, leave input alone
    assertResult(0.0)(wrap(0, -1, 1))
    // at bottom of range, leave input alone
    assertResult(0.0)(wrap(0, 0, 1))
    // at top of range, wrap to bottom
    assertResult(0.0)(wrap(1, 0, 1))
  }

  // ticket #1038
  test("wrap2") {
    assertResult(-0.5)(wrap(-0.5000000000000001, -0.5, 1.5))
  }

  test("wrapPcorPositive") {
    assertResult(0)(wrapPcor(0, 0, 1))
    assertResult(1)(wrapPcor(1, 0, 1))
    assertResult(0)(wrapPcor(2, 0, 1))
    assertResult(1)(wrapPcor(3, 0, 1))
    assertResult(0)(wrapPcor(0, -1, 0))
    assertResult(-1)(wrapPcor(1, -1, 0))
    assertResult(0)(wrapPcor(0, -1, 1))
    assertResult(1)(wrapPcor(1, -1, 1))
    assertResult(-1)(wrapPcor(2, -1, 1))
    assertResult(1)(wrapPcor(-2, -1, 1))
    assertResult(0)(wrapPcor(-3, -1, 1))
    assertResult(0)(wrapPcor(3, -1, 1))
    assertResult(-5)(wrapPcor(6, -5, 5))
    assertResult(5)(wrapPcor(-6, -5, 5))
  }

  // here is how the patch indeces for the world below are laid out.
  // we're pulling from (0,0) which is index 12, with a radius of 1.
  // [  0,  1,  2,  3,  4 ],
  // [  5,| 6,  7,  8,| 9 ], // top
  // [ 10,|11, 12, 13,|14 ], // middle
  // [ 15,|16, 17, 18,|19 ], // botom
  // [ 20, 21, 22, 23, 24 ]

  test("getRegion() for small 2D world") {
    val world = new World2D()
    world.changeTopology(true, true)
    world.createPatches(-2, 2, -2, 2)
    val regions = world.topology.getRegion(0, 0, 1).toArray
    assertResult(Array( (6,9), (11,14), (16,19) ))(regions)
  }

  // [  0,  1,  2,  3,  4 ],
  // [  5,  6,  7,  8,  9 ], // top1
  // [ 10, 11, 12, 13, 14 ], // middle2
  // [ 15, 16, 17, 18, 19 ],
  // [ 20, 21, 22, 23, 24 ],
  // [ 25, 26, 27, 28, 29 ], // middle1, botom2
  // [ 30, 31, 32, 33, 34 ],
  // [ 35, 36, 37, 38, 39 ],
  // [ 40, 41, 42, 43, 44 ],
  // [ 45, 46, 47, 48, 49 ], // bottom1
  // [ 50, 51, 52, 53, 54 ] // top2

  test("getRegion() for rectangular 2D world") {
    val world = new World2D()
    world.changeTopology(true, true)
    world.createPatches(-2, 2, -5, 5)
    val regions1 = world.topology.getRegion(0, 0, 4).toArray
    assertResult(Array( (5,50) ))(regions1)
    val regions2 = world.topology.getRegion(-2, 3, 3).toArray
    assertResult(Array( (0,30), (50,55) ))(regions2)
  }
  //        x       0
  // [  0,  1,  2,  3,| 4,  5,| 6 ] y
  // [  7,  8,  9, 10,|11, 12,|13 ] 0, y + 2
  // [ 14, 15, 16, 17,|18, 19,|20 ] y - 2

  test("getRegion() for thin rectangular 2D world") {
    val world = new World2D()
    world.changeTopology(true, true)
    world.createPatches(-3, 3, -1, 1)
    val regions = world.topology.getRegion(-2, 1, 2).toArray
    assertResult(Array( (0,4), (6,11), (13,18), (20,21) ))(regions)
  }

  //                    x              0       x+5            x-5
  // [   0,   1,   2,   3,   4,   5,   6,   7,   8,|  9,  10,| 11,  12 ]
  // [  13,  14,  15,  16,  17,  18,  19,  20,  21,| 22,  23,| 24,  25 ]
  // [  26,  27,  28,  29,  30,  31,  32,  33,  34,| 35,  36,| 37,  38 ] y
  // [  39,  40,  41,  42,  43,  44,  45,  46,  47,| 48,  49,| 50,  51 ]
  // [  52,  53,  54,  55,  56,  57,  58,  59,  60,| 61,  62,| 63,  64 ] 0
  // [  65,  66,  67,  68,  69,  70,  71,  72,  73,| 74,  75,| 76,  77 ]
  // [  78,  79,  80,  81,  82,  83,  84,  85,  86,| 87,  88,| 89,  90 ] y+5
  // [  91,  92,  93,  94,  95,  96,  97,  98,  99,|100, 101,|102, 103 ] y-5
  // [ 104, 105, 106, 107, 108, 109, 110, 111, 112,|113, 114,|115, 116 ]

  test("getRegion() for large rectangular 2D world") {
    val world = new World2D()
    world.changeTopology(true, true)
    world.createPatches(-6, 6, -4, 4)
    val regions = world.topology.getRegion(-3, 2, 5).toArray
    assertResult(Array( (0,9), (11,22), (24,35), (37,48), (50,61), (63,74), (76,87), (89,100), (102,113), (115,117) ))(regions)
  }

}
