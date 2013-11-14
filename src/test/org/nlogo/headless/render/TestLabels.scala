// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.shape.VectorShape
import org.nlogo.shape.TestHelpers._
import org.nlogo.api.Perspective
import org.nlogo.util.WorldType, WorldType._
import MockGraphics._

class TestPatchLabels extends AbstractTestRenderer {

  Test(Patch(0, 0), LabelSize(10, 10), LabelDrawnAt((221, 221)))
  // because the label size is big, it falls off the screen to the left, and the top.
  // so it should get drawn in all 4 corners.
  Test(Patch(-16, 16), LabelSize(40, 40), LabelDrawnAt((13, 13), (442, 13), (13, 442), (442, 442)))

  // because the label size is small, it doesnt fall off the screen to the left or the top.
  // so it should get drawn in only the top left corner
  Test(Patch(-16, 16), LabelSize(10, 10), LabelDrawnAt((13, 13)))

  // this label is wide, but not tall. it should wrap to the left, but not up.
  Test(Patch(-16, 16), LabelSize(40, 10), LabelDrawnAt((13, 13), (442, 13)))

  // this label is tall, but not wide. it should wrap up, but not to the left.
  Test(Patch(-16, 16), LabelSize(10, 40), LabelDrawnAt((13, 13), (13, 442)))

  // patch on the left, small label that doesnt wrap
  Test(Patch(-16, 0), LabelSize(10, 10), LabelDrawnAt((13, 221)))
  // patch on the left, longer label that should wrap to the right side.
  Test(Patch(-16, 0), LabelSize(40, 10), LabelDrawnAt((13, 221), (442, 221)))

  // patch on the top in the middle, small label that doesnt wrap
  Test(Patch(0, 16), LabelSize(10, 10), LabelDrawnAt((221, 13)))
  // patch on the top in the middle, larger label (in height) should wrap to the bottom.
  Test(Patch(0, 16), LabelSize(10, 40), LabelDrawnAt((221, 13), (221, 442)))

  // patch on right, middle
  Test(Patch(16, 0), LabelSize(50, 50), LabelDrawnAt((429, 221)))
  // patch on bottom, middle
  Test(Patch(0, -16), LabelSize(50, 50), LabelDrawnAt((221, 429)))

  case class Test(p:Patch, labelSize: LabelSize, drawnAt:LabelDrawnAt) extends BaseTest{
    def command = "ask patch "+p.x+" "+p.y+" [set plabel 123]"
    def expectedResults = drawnAt.expectedResults(labelSize)
    override def setup(g:MockGraphics){ g.allowingLabels(labelSize) }
  }
}

class TestPatchLabelsBehaveSameAsTurtleLabels extends AbstractTestRenderer {
  for(wt <- WorldType.all; labelSize <- List(LabelSize(10,10), LabelSize(40,40))) {
    val testName = "Patches vs. Turtles in: " + wt + " " + labelSize
    mockTestUsingWorkspace(testName, radius=16) { workspace =>
      workspace.changeTopology(wt.xWrap, wt.yWrap)
      workspace.command("ask patches [ set plabel 123 sprout 1 [ set label 123 ]]")
      val g = new MockGraphics(this){ allowingLabels(LabelSize(10,10)) }
      workspace.renderer.paint(g, SimpleViewSettings(patchSize = 13))
      g.labels.sorted.grouped(2).toList.foreach{ case Seq(a,b) => assert(a===b) }
    }
  }
}

class TestLabelsAlwaysAppearWhenUsingFollow extends AbstractTestRenderer {
  for(wt <- WorldType.all ) {
    val testName = "Labels appear using FOLLOW in: " + wt
    mockTestUsingWorkspace(testName, radius=2) { workspace =>
      // this should be in the for loop, but cant be because of some mocking crap.
      val g = new MockGraphics(this){ allowingLabels(LabelSize(10,10)) }
      workspace.changeTopology(wt.xWrap, wt.yWrap)
      workspace.command("ask patches [ sprout 1 [set label 123] ]")
      val numTurtles = workspace.report("count turtles").asInstanceOf[Double].toInt
      for(i <- 0 until numTurtles){
        g.clear()
        workspace.command("follow turtle " + i)
        workspace.renderer.paint(g, SimpleViewSettings(patchSize = 13))
        assert(g.labels.size === numTurtles)
      }
    }
  }
}

class TestRendererForTurtleLabels extends AbstractTestRenderer {

  // notice the label is always at patchsize * turtlesize greater than the turtle.
  // that is because a circle is drawn at the top left corner and a label at the bottom right.

  Test(Turtle(at=(0,0), size=1), LabelSize(10,10), TurtleDrawnAt((208, 208)), LabelDrawnAt((221,221)))
  // labels are at the bottom right corner of the turtle.
  // so a bigger turtle should push the label down and to the right.
  Test(Turtle(at=(0,0), size=2), LabelSize(10,10), TurtleDrawnAt((201.5, 201.5)), LabelDrawnAt((227.5,227.5)))

  // top left corner of world
  Test(Turtle(at=(-16,16), size=1), LabelSize(10,10), TurtleDrawnAt((0,0)), LabelDrawnAt((13,13)))
  Test(Turtle(at=(-16,16), size=2), LabelSize(10,10),
    TurtleDrawnAt((-6.5, -6.5),(422.5, -6.5),(-6.5, 422.5),(422.5, 422.5)), LabelDrawnAt((19.5,19.5)))
  Test(Turtle(at=(-16,16), size=2), LabelSize(40,40),
    TurtleDrawnAt((-6.5, -6.5),(422.5, -6.5),(-6.5, 422.5),(422.5, 422.5)),
    LabelDrawnAt((19.5,19.5),(448.5,19.5),(19.5,448.5),(448.5,448.5)))

  case class Test(t:Turtle, labelSize: LabelSize, turtleDrawnAt:TurtleDrawnAt, labelDrawnAt:LabelDrawnAt) extends BaseTest{
    def command =
      "crt 1[ set shape \"circle\" setxy " + t.at._1 + " " + t.at._2 + " set size " + t.size + " set label 123]"
    def expectedResults = turtleDrawnAt.expectedResults(t.size) ::: labelDrawnAt.expectedResults(labelSize)
    override def setup(g:MockGraphics){ g.allowingLabels(labelSize) }
  }
}
