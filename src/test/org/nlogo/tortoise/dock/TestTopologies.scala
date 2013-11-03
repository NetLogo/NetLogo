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
    val world = new WorldDimensions(-10, 10, -10, 10, 12.0, false, false);
    testDistance(world)
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
    val world = new WorldDimensions(-10, 10, -10, 10, 12.0, false, false);
    testDistancexy(world)
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
    val world = new WorldDimensions(-4, 4, -4, 4, 12.0, false, false);
    testEdgeWrapping(world)
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
    val world = new WorldDimensions(-4, 4, -4, 4, 12.0, false, false);
    testDiffuse(world)
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
}
