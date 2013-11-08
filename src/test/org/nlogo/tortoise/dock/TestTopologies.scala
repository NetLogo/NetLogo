// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api, api.WorldDimensions

class TestTopologies extends DockingSuite {
  def testDistance(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("cro 8")
    testCommand("ask turtles [ fd 5 ]")
    testCommand("ask turtles [ output-print distance turtle 0 ]")
    testCommand("ask turtles [ output-print distance patch 0 0 ]")
    testCommand("ask patches [ output-print distance turtle 0 ]")
    testCommand("ask patches [ output-print distance patch 0 0 ]")
    testCommand("ask turtles [ fd 3 ]")
    testCommand("ask turtles [ output-print distance turtle 0 ]")
    testCommand("ask turtles [ output-print distance patch 0 0 ]")
    testCommand("ask patches [ output-print distance turtle 0 ]")
  }

  test("torus distance") { implicit fixture => import fixture._
    testDistance(WorldDimensions.square(10))
  }

  test("box distance") { implicit fixture => import fixture._
    testDistance(new WorldDimensions(-10, 10, -10, 10, 12.0, false, false))
  }

  test("vertcyl distance") { implicit fixture => import fixture._
    testDistance(new WorldDimensions(-10, 10, -10, 10, 12.0, false, true))
  }

  def testDistancexy(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("cro 8")
    testCommand("ask turtles [ fd 1 ]")
    testCommand("ask turtles [ output-print distancexy 1.2 2.3 ]")
    testCommand("ask patches [ output-print distancexy 1.2 2.3 ]")
    testCommand("ask turtles [ fd 5 ]")
    testCommand("ask turtles [ output-print distancexy -2.2 -5.3 ]")
  }

  test("torus distancexy") { implicit fixture => import fixture._
    testDistancexy(WorldDimensions.square(10))
  }

  test("box distancexy") { implicit fixture => import fixture._
    testDistancexy(new WorldDimensions(-10, 10, -10, 10, 12.0, false, false))
  }

  test("vertcyl distancexy") { implicit fixture => import fixture._
    testDistancexy(new WorldDimensions(-10, 10, -10, 10, 12.0, false, true))
  }

  def testSetxyWraps(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("crt 20")
    testCommand("ask turtles [ setxy random 20 random 20 ]")
    testCommand("ask turtles [ set xcor random 20 ]")
    testCommand("ask turtles [ set ycor random 20 ]")
  }

  test("torus setxy wraps") { implicit fixture => import fixture._
    testSetxyWraps(WorldDimensions.square(10))
  }

  test("box setxy wraps") { implicit fixture => import fixture._
    testSetxyWraps(new WorldDimensions(-10, 10, -10, 10, 12.0, false, false))
  }

  test("vertcyl setxy wraps") { implicit fixture => import fixture._
    testSetxyWraps(new WorldDimensions(-10, 8, -9, 5, 12.0, false, true))
  }

  def testEdgeWrapping(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("cro 20")
    testCommand("ask turtles [ fd 6 ]")
  }

  test("torus edge wrapping") { implicit fixture => import fixture._
    testEdgeWrapping(WorldDimensions.square(4))
  }

  test("box edge wrapping") { implicit fixture => import fixture._
    testEdgeWrapping(new WorldDimensions(-4, 4, -4, 4, 12.0, false, false))
  }

  test("vertcyl edge wrapping") { implicit fixture => import fixture._
    testEdgeWrapping(new WorldDimensions(-4, 4, -4, 4, 12.0, false, true))
  }

  def testDiffuse(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("patches-own [ chemical ]", world)
    testCommand("ask patches [ set chemical (random 168) / ((random 24) + 1) ]")
    compare("[ chemical ] of patches")
    testCommand("diffuse chemical 0.6")
    compare("[ chemical ] of patches")
    testCommand("diffuse chemical 0.6")
    compare("[ chemical ] of patches")
    testCommand("diffuse chemical 0.6")
    compare("[ chemical ] of patches")
    testCommand("diffuse chemical .99")
    compare("[ chemical ] of patches")
  }

  test("torus diffuse") { implicit fixture => import fixture._
    testDiffuse(WorldDimensions.square(4))
  }

  test("box diffuse") { implicit fixture => import fixture._
    testDiffuse(new WorldDimensions(-4, 4, -4, 4, 12.0, false, false))
  }

  test("vertcyl diffuse") { implicit fixture => import fixture._
    testDiffuse(new WorldDimensions(-4, 4, -4, 4, 12.0, false, true))
  }

  def testNeighbors(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("""ask patches [ ask neighbors [ output-print self ]]""")
    testCommand("""ask patches [ ask neighbors4 [ output-print self ]]""")
    testCommand("ask patches [ sprout 1 ]")
    testCommand("""ask turtles [ ask neighbors4 [ output-print self ]]""")
  }

  test("torus neighbors") { implicit fixture => import fixture._
    testNeighbors(WorldDimensions.square(4))
  }

  test("box neighbors") { implicit fixture => import fixture._
    testNeighbors(new WorldDimensions(-4, 4, -4, 4, 12.0, false, false))
  }

  test("vertcyl neighbors") { implicit fixture => import fixture._
    testNeighbors(new WorldDimensions(-4, 4, -4, 4, 12.0, false, true))
  }

  def testFace(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("ask patches [ sprout 1 ]")
    for(i <- 1 to 80)
      testCommand(s"ask turtles [ face turtle $i ]")
    for(x <- -4 to 4)
      for(y <- -4 to 4)
        testCommand(s"ask turtles [ face patch $x $y ]")
    for(_ <- 0 to 10)
      testCommand("ask turtles [ facexy ((random 8) / ((random 8) + 1) - 4) ((random 8) / ((random 8) + 1) - 4) ]")
  }

  test("torus face") { implicit fixture => import fixture._
    testFace(WorldDimensions.square(4))
  }

  test("box face") { implicit fixture => import fixture._
    testFace(new WorldDimensions(-4, 4, -4, 4, 12.0, false, false))
  }

  test("vertcyl face") { implicit fixture => import fixture._
    testFace(new WorldDimensions(-4, 4, -4, 4, 12.0, false, true))
  }

  def testLinkWraps(world: WorldDimensions)(implicit fixture: DockingFixture) : Unit = { import fixture._
    declare("", world)
    testCommand("ask patch 3 0 [ sprout 1 ]")
    testCommand("ask patch -3 0 [ sprout 1 ]")
    testCommand("ask patch 0 -3 [ sprout 1 ]")
    testCommand("ask patch 0 0 [ sprout 1 ]")
    testCommand("ask patch 0 3 [ sprout 1 ]")
    testCommand("ask turtles [ create-links-with other turtles ]")
    testCommand("ask turtles [ set xcor xcor - 1 ]")
  }

  test("torus link wraps") { implicit fixture => import fixture._
    testLinkWraps(WorldDimensions.square(4))
  }

  test("box link wraps") { implicit fixture => import fixture._
    testLinkWraps(new WorldDimensions(-4, 4, -4, 4, 12.0, false, false))
  }

  test("vertcyl link wraps") { implicit fixture => import fixture._
    testLinkWraps(new WorldDimensions(-4, 4, -4, 4, 12.0, false, true))
  }
}
