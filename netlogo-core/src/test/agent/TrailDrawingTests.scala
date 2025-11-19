// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.TrailDrawerInterface
import org.nlogo.core.WorldDimensions
import org.nlogo.util.AnyFunSuiteEx

// This test is used primarily to verify exact numeric reproducibility between NetLogo desktop
// and NetLogo Web. If there are discrepancies found between the two in the area of trail-drawing
// new tests should be added here.
class TrailDrawingTests extends AnyFunSuiteEx {

  case class LineSeg(x0: Double, y0: Double, x1: Double, y1: Double, color: AnyRef, size: Double, mode: String)
  class DummyTrailDrawer extends TrailDrawerInterface {
    var lines = Seq[LineSeg]()
    def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: AnyRef, size: Double, mode: String): Unit = {
      lines :+= LineSeg(x0, y0, x1, y1, color, size, mode)
    }
    def colors: Array[Int] = ???
    def getHeight: Int = ???
    def getWidth: Int = ???
    def isBlank: Boolean = ???
    def markClean(): Unit = ???
    def markDirty(): Unit = ???
    def setColors(colors: Array[Int], width: Int, height: Int) = ???
    def getDrawing: AnyRef = ???
    def sendPixels: Boolean = ???
    def sendPixels(dirty: Boolean) = ???
    def stamp(agent: org.nlogo.api.Agent, erase: Boolean) = ???
    @throws(classOf[java.io.IOException])
    def readImage(is: java.awt.image.BufferedImage) = ???
    @throws(classOf[java.io.IOException])
    def readImage(is: java.io.InputStream) = ???
    @throws(classOf[java.io.IOException])
    def importDrawing(is: java.io.InputStream, mimeType: Option[String] = None) = ???
    @throws(classOf[java.io.IOException])
    def importDrawing(file: org.nlogo.core.File) = ???
    def importDrawingBase64(base64: String) = ???
    def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage = ???
    def clearDrawing() = ???
    def exportDrawingToCSV(writer: java.io.PrintWriter) = ???
    def rescaleDrawing() = ???
    def isDirty: Boolean = ???
  }

  val worldSquare = new WorldDimensions(0, 0, 0, 0)

  def makeWorld(dimensions: WorldDimensions) =
    new World2D() {
      createPatches(dimensions)
      realloc()
    }

  def makeTurtle(world: World, cors: Array[Int]) =
    new Turtle2D(world, world.turtles, cors(0).toDouble, cors(1).toDouble)

  trait Helper {
    val drawer = new DummyTrailDrawer()
    val world = makeWorld(worldSquare)
    world.trailDrawer(drawer)
    val turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
  }

  trait HelperBig {
    val drawer = new DummyTrailDrawer()
    val world = makeWorld(new WorldDimensions(-4, 4, -4, 4))
    world.trailDrawer(drawer)
    val turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
  }

  test("no pen trails when turtle doesn't move") { new Helper {
    assert(drawer.lines.isEmpty)
  } }

  test("single pen trail when turtle moves without crossing world boundary") { new Helper {
    turtle.xandycor(0.25, 0.25)
    assert(drawer.lines.length == 1)
    assertResult(LineSeg(0.0, 0.0, 0.25, 0.25, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines.head)
  } }

  test("single pen trail when turtle moves backwards without crossing world boundary") { new Helper {
    turtle.heading(0)
    turtle.jump(-0.25)
    assert(drawer.lines.length == 1)
    assertResult(LineSeg(0.0, 0.0, 0, -0.25, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines.head)
  } }

  test("draws two trails when turtle crosses world boundary") { new Helper {
    turtle.heading(0)
    turtle.jump(0.75)
    assert(drawer.lines.length == 2)
    assertResult(LineSeg(0, 0, 0, 0.5, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(0))
    assertResult(LineSeg(0, -0.5, 0, -0.25, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(1))
  } }

  test("draws one trail with face then moveTo") { new HelperBig {
    val target = world.getPatchAt(-5, 5)
    turtle.moveToPatchCenter()
    turtle.face(target, true)
    turtle.moveTo(target)
    assert(drawer.lines.length == 1)
    assertResult(LineSeg(0.0, 0.0, 4.0, -4.0, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(0))
  } }

  test("draws three trails with setxy and wrapping") { new HelperBig {
    turtle.penMode(Turtle.PEN_UP)
    turtle.xandycor(3.75, 3.5)
    assert(turtle.xcor == 3.75)
    assert(turtle.ycor == 3.5)
    turtle.penMode(Turtle.PEN_DOWN)
    turtle.xandycor(turtle.shortestPathX(-3.75), turtle.shortestPathY(-3))
    assert(drawer.lines.length == 3)
    assertResult(Seq(
      LineSeg(3.75, 3.5, 4.35, 4.5, Double.box(0.0), 1.0, Turtle.PEN_DOWN),
      LineSeg(4.35, -4.5, 4.5, -4.249999999999999, Double.box(0.0), 1.0, Turtle.PEN_DOWN),
      LineSeg(-4.5, -4.249999999999999, -3.750000000000001, -2.999999999999999, Double.box(0.0), 1.0, Turtle.PEN_DOWN)
    ))(drawer.lines)
  } }

  test("draws three trails when turtle crosses world boundary twice") { new Helper {
    turtle.heading(319)
    turtle.jump(1)
    assert(drawer.lines.length == 3)
    assertResult(LineSeg(0.0, 0.0, -0.43464336890811345, 0.5, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(0))
    assertResult(LineSeg(-0.43464336890811345, -0.5, -0.5, -0.4248157963894954, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(1))
    assertResult(LineSeg(0.5, -0.4248157963894954, 0.3439409710094926, -0.24529041977722812, Double.box(0.0), 1.0, Turtle.PEN_DOWN))(drawer.lines(2))
  } }

  test("draws between opposite corners without infinite loop") {
    // bottom left origin
    val drawer = new DummyTrailDrawer()
    var world = makeWorld(new WorldDimensions(0, 4, 0, 4))
    world.trailDrawer(drawer)
    var turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
    turtle.xandycor(4.5, 4.5)

    // bottom right origin
    world = makeWorld(new WorldDimensions(-4, 0, 0, 4))
    world.trailDrawer(drawer)
    turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
    turtle.xandycor(-4.5, 4.5)

    // top left origin
    world = makeWorld(new WorldDimensions(0, 4, -4, 0))
    world.trailDrawer(drawer)
    turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
    turtle.xandycor(4.5, -4.5)

    // top right origin
    world = makeWorld(new WorldDimensions(-4, 0, -4, 0))
    world.trailDrawer(drawer)
    turtle = makeTurtle(world, Array(0, 0))
    turtle.penMode(Turtle.PEN_DOWN)
    turtle.xandycor(-4.5, -4.5)
  }
}
