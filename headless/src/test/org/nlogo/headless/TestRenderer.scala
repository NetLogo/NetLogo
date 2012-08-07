// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.shape.VectorShape
import org.nlogo.shape.TestHelpers._
import org.nlogo.api.Perspective
import MockGraphics._
import org.nlogo.util.WorldType
import org.nlogo.util.WorldType._

// These tests were written before render.TestTurtleDrawer existed.  If you need to test rendering
// stuff, consider doing it in the render package rather than here, using TestTurtleDrawer or your
// or own test scaffold that uses similar techniques to keep it all within the render package.
// - ST 6/24/10

// however, sometimes its easier to have the entire workspace available so that you can issue commands
// and test that the right things were rendered. if you need that, its easier here.
// JC 7/6/10
class TestRenderer extends AbstractTestRenderer {

  testUsingWorkspace("One Turtle Default") { workspace =>
    workspace.command("crt 1")
    val g = new MockGraphics(this)
    workspace.renderer.resetCache( 12.0 )
    workspace.renderer.paint(g,workspace)
    testOperations(g, List(
        Rect(Location(0.0, 0.0), Size(132.0,132.0), filled=true),
        Image(Location(60.0, 60.0), Size(12.0,12.0))))
  }

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

  testUsingWorkspace("More links", radius=16){ workspace: HeadlessWorkspace =>
    workspace.command("create-nodes 2 [ ht setxy ((who - 1) * 5) 0 ] ask node 0 [ create-link-with node 1 ]")
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

class TestColorRendering extends AbstractTestRenderer {

  testUsingWorkspace("non-recolorable shapes dont respond to 'set color' with no alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = false))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15]")
    // 255, 255, 255 is white
    workspace.testColors(255, 255, 255)
    workspace.command("ask turtles [ set color green ]")
    // this shape is not recolorable, everything should still be white.
    workspace.testColors(255, 255, 255)
  }

  testUsingWorkspace("non-recolorable shapes do respond to 'set color' with alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = false))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15]")
    workspace.testColors(255, 255, 255)
    // set color to a random color with a definite alpha value.
    // the color change shouldnt take effect, because the shape is recolorable.
    // but the alpha change should.
    workspace.command("ask turtles [ set color (list random 255 random 255 random 255 127) ]")
    //workspace.dumpWorldToImage()
    workspace.testColors(127,127,127,255)
  }

  testUsingWorkspace("recolorable shapes respond to 'set color'"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = true))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15 set color white]")
    // 255, 255, 255 is white
    workspace.testColors(255, 255, 255)
    workspace.command("ask turtles [ set color red ]")
    // this shape is recolorable, everything should now be red
    //workspace.dumpWorldToImage()
    workspace.testColors(215, 50, 41)
  }

  testUsingWorkspace("recolorable shapes also respond to 'set color' with alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = true))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15 set color red]")
    workspace.testColors(215, 50, 41)
    workspace.command("ask turtles [ set color [255 255 255 127] ]")
    //workspace.dumpWorldToImage()
    workspace.testColors(127,127,127,255)
  }

  class RichWorkspace(workspace: HeadlessWorkspace){
    def setShapes(shapes: VectorShape*) {
      // remove all shapes from the world
      import collection.JavaConverters._
      for (shape <- workspace.world.turtleShapeList().getShapes.asScala)
        workspace.world.turtleShapeList().removeShape(shape)
      // add one non-recolorable shape
      shapes.foreach(workspace.world.turtleShapeList().add)
    }
    // test that the entire world is a particular color.
    def testColors(r: Int, g: Int, b: Int, a: Int = 255) {
      val image = workspace.exportView
      // why cut off the borders here?
      // a turtle of size 15 almost fills the screen, but leaves some black border. remove it.
      // a turtle of size 16 overlaps some and then doubles alpha values makes things more confusing.
      for (i <- 5 until image.getSampleModel.getWidth - 5; j <- 5 until image.getSampleModel.getHeight - 5) {
        val pixelColor = java.awt.Color.decode(image.getRGB(i, j).toString)
        //println((i, j) + " " + (pixelColor.getRed,pixelColor.getGreen,pixelColor.getBlue,pixelColor.getAlpha))
        assert((pixelColor.getRed, pixelColor.getGreen, pixelColor.getBlue, pixelColor.getAlpha) === (r, g, b, a))
      }
    }
    // for sanity only, dump the world to an image in order to look at it.
    def dumpWorldToImage(){
      workspace.command("export-view \"test.png\"")
    }
  }

  implicit def EnrichWorkspace(workspace: HeadlessWorkspace): RichWorkspace = new RichWorkspace(workspace)
}

class TestRendererForPatchLabels extends AbstractTestRenderer{

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

class TestRendererForTurtleLabels extends AbstractTestRenderer{

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

class TestLabelsAlwaysAppearWhenUsingFollow extends AbstractTestRenderer{
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

class TestRendererPatchLabelsBehaveSameAsTurtleLabels extends AbstractTestRenderer{
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

abstract class AbstractTestRenderer(worldType: WorldType = Torus) extends TestUsingWorkspace {

  type Point = (Double,Double)
  case class TurtleDrawnAt(ps:Point*){
    def expectedResults(turtleSize:Int) = (for (p <- ps) yield {
      // this 13 here is really patch size....should probably be fixed.
      Circle(Location(p._1.toDouble,p._2.toDouble), Size(13.0 * turtleSize,13.0 * turtleSize), filled=true)
    }).toList
  }
  case class LabelDrawnAt(ps:Point*){
    def expectedResults(labelSize: LabelSize) = (for(p<-ps) yield Label(Location(p._1.toDouble, p._2.toDouble))).toList
  }
  case class Patch(x: Int, y:Int)
  object Origin extends Patch(0,0)
  case class Turtle(at:Point, size:Int=1)

  abstract class BaseTest {
    run()
    def command: String
    def expectedResults: List[Operation]
    def setup(g:MockGraphics){}
    def run(){
      mockTestUsingWorkspace(this.toString, radius = 16, worldType = AbstractTestRenderer.this.worldType) { workspace =>
        val g = new MockGraphics(AbstractTestRenderer.this)
        setup(g)
        when{
          workspace.command(command)
          workspace.renderer.paint(g, SimpleViewSettings(patchSize = 13))
          testOperations(g, List(Rect(Location(0.0,0.0), Size(429.0,429.0), filled=true)) ::: expectedResults.toList)
        }
      }
    }
  }

  def testOperations(g: MockGraphics, expecteds: List[Operation]) {
//    info("expected: " + expecteds.mkString("\n"))
//    info("actual: " + g.operations.mkString("\n"))
    assert(expecteds.mkString("\n") === g.toString)
  }
}
