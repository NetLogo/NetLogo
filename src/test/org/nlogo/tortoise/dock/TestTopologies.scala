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
}
