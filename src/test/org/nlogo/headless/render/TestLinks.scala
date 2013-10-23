// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.shape.TestHelpers._
import org.nlogo.api.Perspective
import MockGraphics._

class TestLinks extends AbstractTestRenderer {

  testUsingWorkspace("Links") { workspace: HeadlessWorkspace =>
    workspace.command("cro 2 [ create-links-with other turtles fd 1 ]")
    val g = new MockGraphics(this)
    workspace.renderer.resetCache( 12.0 )
    workspace.renderer.paint(g,workspace)
    testOperations(g,List(Rect(Location(0.0, 0.0), Size(132.0,132.0), filled=true),
                          Line(Location(66.0, 54.0),Location(66.0, 78.0)),
                          Image(Location(60.0, 48.0), Size(12.0, 12.0)),
                          Image(Location(60.0, 72.0), Size(12.0, 12.0))))
  }

  testUsingWorkspace("More links", radius=16){ workspace: HeadlessWorkspace =>
    workspace.command("create-turtles 2 [ ht setxy ((who - 1) * 5) 0 ] ask turtle 0 [ create-link-with turtle 1 ]")
    val g = new MockGraphics(this)
    workspace.renderer.paint(g,
      SimpleViewSettings(patchSize=61.285714285714285, viewOffsetX=13,viewOffsetY= -13, renderPerspective=true, perspective=Perspective.Follow))
    testOperations(g,List(
      Rect(Location(0.0, 0.0), Size(2022.0,2022.0), filled=true),
      Line(Location(-91.92857142857143,214.5),Location(214.5,214.5))))
  }

  testUsingWorkspace("Draw link with no mini view", radius=10){ workspace: HeadlessWorkspace =>
    workspace.command("create-turtles 1 [ ht setxy -6 0 ] " +
                               "create-turtles 1 [ ht setxy 6 0 ] " +
                               "ask turtle 0 [ create-link-with turtle 1 ]")
    val g = new MockGraphics(this)
    workspace.renderer.paint(g, SimpleViewSettings(patchSize=10, viewOffsetX=0,viewOffsetY=0))
    testOperations(g,List(
      Rect(Location(0.0,0.0), Size(210.0,210.0), filled=true),
      Line(Location(45.0,105.0),Location(-45.0,105.0)),
      Line(Location(255.0,105.0),Location(165.0,105.0))))
  }

  testUsingWorkspace("Links in Mini view (10)", radius=10){ workspace: HeadlessWorkspace =>
    workspace.command("create-turtles 1 [ ht setxy -6 0 ] " +
                               "create-turtles 1 [ ht setxy 6 0 ] " +
                               "ask turtle 0 [ create-link-with turtle 1 ]")
    val g = new MockGraphics(this)
    workspace.renderer.paint(g, SimpleViewSettings(patchSize=10, viewOffsetX=3,viewOffsetY= -3,
      renderPerspective=true, perspective=Perspective.Follow))
    testOperations(g,List(
      Rect(Location(0.0,0.0), Size(210.0,210.0), filled=true),
      Line(Location(15.0,75.0),Location(-75.0,75.0)),
      Line(Location(225.0,75.0),Location(135.0,75.0))))
  }

}
