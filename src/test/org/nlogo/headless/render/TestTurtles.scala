// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.shape.VectorShape
import org.nlogo.shape.TestHelpers._
import org.nlogo.util.WorldType, WorldType._
import MockGraphics._

class TestTurtles extends AbstractTestRenderer {

  testUsingWorkspace("One Turtle Default") { workspace =>
    workspace.command("crt 1")
    val g = new MockGraphics(this)
    workspace.renderer.resetCache( 12.0 )
    workspace.renderer.paint(g,workspace)
    testOperations(g, List(
        Rect(Location(0.0, 0.0), Size(132.0,132.0), filled=true),
        Image(Location(60.0, 60.0), Size(12.0,12.0))))
  }

  testUsingWorkspace("Rectangle1") { workspace =>
    workspace.command("cro 1 [ set size 3 set shape \"square\" ]")
    val g = new MockGraphics(this)
    workspace.renderer.paint(g,workspace)
    testOperations(g,List(
      Rect(Location(0.0, 0.0), Size(132.0,132.0), filled=true),
      // gross.  floating point math.  these answers should be 28.8. ev 4/14/09
      Rect(Location(51.6,51.6), Size(28.799999999999997,28.799999999999997), filled=true)))
  }

  testUsingWorkspace("Rectangle2") { workspace =>
    workspace.command("cro 1 [ set size 4 set shape \"square\" ]")
    val g = new MockGraphics(this)
    workspace.renderer.paint(g,workspace)
    testOperations(g,List(
      Rect(Location(0.0, 0.0), Size(132.0,132.0), filled=true),
      Rect(Location(46.8, 46.8), Size(38.4,38.4), filled=true)))
  }

}


class TestTorusRendererForTurtles extends AbstractTestRenderer(Torus){

  Test(Turtle(at=(0,0), size=1), TurtleDrawnAt((208, 208)))
  Test(Turtle(at=(5,5), size=1), TurtleDrawnAt((273, 143)))
  Test(Turtle(at=(-5,5), size=1), TurtleDrawnAt((143, 143)))
  // top left
  Test(Turtle(at=(-16,16), size=1), TurtleDrawnAt((0, 0))) // circles are drawn from their top left corner.
  Test(Turtle(at=(-16,16), size=2), TurtleDrawnAt((-6.5, -6.5),(422.5, -6.5),(-6.5, 422.5),(422.5, 422.5)))
  // bottom left
  Test(Turtle(at=(-16,-16), size=1), TurtleDrawnAt((0, 416)))
  Test(Turtle(at=(-16,-16), size=2), TurtleDrawnAt((-6.5, 409.5),(422.5, 409.5),(-6.5, -19.5),(422.5, -19.5)))
  // bottom right
  Test(Turtle(at=(16,-16), size=1), TurtleDrawnAt((416, 416)))
  Test(Turtle(at=(16,-16), size=2), TurtleDrawnAt((409.5, 409.5),(-19.5, 409.5),(409.5, -19.5),(-19.5, -19.5)))
  // top right
  Test(Turtle(at=(16,16), size=1), TurtleDrawnAt((416, 0)))
  Test(Turtle(at=(16,16), size=2), TurtleDrawnAt((409.5, -6.5),(-19.5, -6.5),(409.5, 422.5),(-19.5, 422.5)))

  case class Test(t:Turtle, drawnAt:TurtleDrawnAt) extends BaseTest{
    def command = "crt 1[ set shape \"circle\" setxy " + t.at._1 + " " + t.at._2 + " set size " + t.size + " ]"
    def expectedResults = drawnAt.expectedResults(t.size)
  }
}

class TestBoxRendererForTurtles extends AbstractTestRenderer(Box){

  Test(Turtle(at=(0,0), size=1), TurtleDrawnAt((208, 208)))
  Test(Turtle(at=(5,5), size=1), TurtleDrawnAt((273, 143)))
  Test(Turtle(at=(-5,5), size=1), TurtleDrawnAt((143, 143)))
  // top left
  Test(Turtle(at=(-16,16), size=1), TurtleDrawnAt((0, 0)))
  Test(Turtle(at=(-16,16), size=2), TurtleDrawnAt((-6.5, -6.5)))
  // bottom left
  Test(Turtle(at=(-16,-16), size=1), TurtleDrawnAt((0, 416)))
  Test(Turtle(at=(-16,-16), size=2), TurtleDrawnAt((-6.5, 409.5)))
  // bottom right
  Test(Turtle(at=(16,-16), size=1), TurtleDrawnAt((416, 416)))
  Test(Turtle(at=(16,-16), size=2), TurtleDrawnAt((409.5, 409.5)))
  // top right
  Test(Turtle(at=(16,16), size=1), TurtleDrawnAt((416, 0)))
  Test(Turtle(at=(16,16), size=2), TurtleDrawnAt((409.5, -6.5)))

  case class Test(t:Turtle, drawnAt:TurtleDrawnAt) extends BaseTest{
    def command = "crt 1[ set shape \"circle\" setxy " + t.at._1 + " " + t.at._2 + " set size " + t.size + " ]"
    def expectedResults = drawnAt.expectedResults(t.size)
  }
}

class TestVerticalCylRendererForTurtles extends AbstractTestRenderer(VerticalCyl){

  Test(Turtle(at=(0,0), size=1), TurtleDrawnAt((208, 208)))
  Test(Turtle(at=(5,5), size=1), TurtleDrawnAt((273, 143)))
  Test(Turtle(at=(-5,5), size=1), TurtleDrawnAt((143, 143)))
  // top left
  Test(Turtle(at=(-16,16), size=1), TurtleDrawnAt((0, 0)))
  Test(Turtle(at=(-16,16), size=2), TurtleDrawnAt((-6.5, -6.5),(422.5, -6.5)))
  // bottom left
  Test(Turtle(at=(-16,-16), size=1), TurtleDrawnAt((0, 416)))
  Test(Turtle(at=(-16,-16), size=2), TurtleDrawnAt((-6.5, 409.5),(422.5, 409.5)))
  // bottom right
  Test(Turtle(at=(16,-16), size=1), TurtleDrawnAt((416, 416)))
  Test(Turtle(at=(16,-16), size=2), TurtleDrawnAt((409.5, 409.5),(-19.5, 409.5)))
  // top right
  Test(Turtle(at=(16,16), size=1), TurtleDrawnAt((416, 0)))
  Test(Turtle(at=(16,16), size=2), TurtleDrawnAt((409.5, -6.5),(-19.5, -6.5)))

  case class Test(t:Turtle, drawnAt:TurtleDrawnAt) extends BaseTest{
    def command = "crt 1[ set shape \"circle\" setxy " + t.at._1 + " " + t.at._2 + " set size " + t.size + " ]"
    def expectedResults = drawnAt.expectedResults(t.size)
  }
}

class TestHorizontalCylRendererForTurtles extends AbstractTestRenderer(HorizontalCyl){

  Test(Turtle(at=(0,0), size=1), TurtleDrawnAt((208, 208)))
  Test(Turtle(at=(5,5), size=1), TurtleDrawnAt((273, 143)))
  Test(Turtle(at=(-5,5), size=1), TurtleDrawnAt((143, 143)))
  // top left
  Test(Turtle(at=(-16,16), size=1), TurtleDrawnAt((0, 0)))
  Test(Turtle(at=(-16,16), size=2), TurtleDrawnAt((-6.5, -6.5),(-6.5, 422.5)))
  // bottom left
  Test(Turtle(at=(-16,-16), size=1), TurtleDrawnAt((0, 416)))
  Test(Turtle(at=(-16,-16), size=2), TurtleDrawnAt((-6.5, 409.5),(-6.5, -19.5)))
  // bottom right
  Test(Turtle(at=(16,-16), size=1), TurtleDrawnAt((416, 416)))
  Test(Turtle(at=(16,-16), size=2), TurtleDrawnAt((409.5, 409.5),(409.5,-19.5)))
  // top right
  Test(Turtle(at=(16,16), size=1), TurtleDrawnAt((416, 0)))
  Test(Turtle(at=(16,16), size=2), TurtleDrawnAt((409.5, -6.5),(409.5, 422.5)))

  case class Test(t:Turtle, drawnAt:TurtleDrawnAt) extends BaseTest{
    def command = "crt 1[ set shape \"circle\" setxy " + t.at._1 + " " + t.at._2 + " set size " + t.size + " ]"
    def expectedResults = drawnAt.expectedResults(t.size)
  }
}
