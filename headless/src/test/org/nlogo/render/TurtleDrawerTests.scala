// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import org.nlogo.api.{AgentKind, AgentSet, Color, GraphicsInterface, ShapeList, Turtle, World}
import org.nlogo.util.MockSuite

class TurtleTestsDrawer extends MockSuite {

  val cachableSizes = List(1,1.5,2)
  val testSizes = List(0.5, 1, 1.1, 1.5, 1.75, 2, 5)
  val colors@List(white, red, blue, green) = List("white", "red", "blue", "green").map(color)

  // these tests have the same results independent of recolorable and size
  for(recolorable<-List(true, false); size<-testSizes){
    mockTest("first draw should add shape to cache (recolorable:"+recolorable+") (size:"+size+") ") {
      val tester = CacheTester(testShapeIsRecolorable = recolorable, TestTurtle(size=size))
      assert(tester.cacheSize === (if(cachableSizes contains size) 1 else 0))
    }

    mockTest("second draw should not add shape to cache (recolorable:"+recolorable+") (size:"+size+")") {
      val turtle = TestTurtle(size=size)
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtle, turtle)
      assert(tester.cacheSize === (if(cachableSizes contains size) 1 else 0))
    }

    mockTest("drawing same shape, same color, but different alpha should " +
            "add shape to cache (recolorable:"+recolorable+") (size:"+size+")") {
      val turtle1 = TestTurtle(size=size, color = red)
      val turtle2 = TestTurtle(size=size, color = withAlpha(red, 127))
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtle1, turtle2)
      assert(tester.cacheSize == (if(cachableSizes contains size) 2 else 0))
    }

    mockTest("drawing same shape, same color, but different alpha should " +
            "add shape to cache (2) (recolorable:"+recolorable+") (size:"+size+")") {
      val turtle1 = TestTurtle(size=size, color = withAlpha(red, 127))
      val turtle2 = TestTurtle(size=size, color = withAlpha(red, 128))
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtle1, turtle2)
      assert(tester.cacheSize == (if(cachableSizes contains size) 2 else 0))
    }

    mockTest("second draw with same alpha should not add " +
            "shape to cache (recolorable:"+recolorable+") (size:"+size+")") {
      val turtle = TestTurtle(size=size, color = withAlpha(red, 127))
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtle, turtle)
      assert(tester.cacheSize === (if(cachableSizes contains size) 1 else 0))
    }
  }

  // these two tests are the same, but act differently if the shape is not recolorable
  // if its not recolorable, then changing its color shouldnt add a new shape to the cache
  // because color doesnt matter in that case.
  for(size<-testSizes){
    mockTest("drawing same shape, but different color should " +
            "add shape to cache (recolorable:true) (size:"+size+")") {
      val turtles = colors.map(c => TestTurtle(size=size, color=c))
      val tester = CacheTester(testShapeIsRecolorable = true, turtles:_*)
      assert(tester.cacheSize == (if(cachableSizes contains size) turtles.size else 0))
    }
    mockTest("drawing same shape, but different color should NOT! " +
            "add shape to cache when (recolorable:false) (size:" + size + ")") {
      val turtles = colors.map(c => TestTurtle(size=size, color=c))
      val tester = CacheTester(testShapeIsRecolorable = false, turtles:_*)
      // we should only add one shape to cache, regardless of how many turtles there are.
      assert(tester.cacheSize == (if (cachableSizes contains size) 1 else 0))
    }
  }

  // these tests work the same independent of recolorability
  // they test adding things to the cache based on size alone.
  for(recolorable<-List(true, false)) {
    mockTest("drawing same shape, but three different cachable sizes should " +
            "add shapes to cache (recolorable:"+recolorable+")") {
      val turtles = cachableSizes.map(s => TestTurtle(size = s))
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtles:_*)
      assert(tester.cacheSize == 3)
    }
    mockTest("drawing same shape, but lots of different sizes should " +
            "add some shapes to cache (recolorable:"+recolorable+")") {
      val turtles = testSizes.map(s => TestTurtle(size = s)) // only 3 of these sizes are cachable
      val tester = CacheTester(testShapeIsRecolorable = recolorable, turtles:_*)
      assert(tester.cacheSize == 3) // so only 3 shapes should be in the cache.
    }
  }

  ///
  def color(name: String): java.lang.Double =
    Double.box(Color.getColorNumberByIndex(Color.getColorNamesArray.indexOf(name)))
  def withAlpha(boxedColor: java.lang.Double, alpha: Int) =
    Color.getRGBListByARGB(Color.getRGBInt(boxedColor)).lput(Double.box(alpha))

  case class CacheTester(testShapeIsRecolorable: Boolean, turtles: TestTurtle*) {
    val patchSize = 13
    def makeTurtleDrawer = {
      import org.nlogo.shape.TestHelpers._
      new TurtleDrawer(
        new ShapeList(
          AgentKind.Turtle,
          makeSquarePolygon(recolorable = testShapeIsRecolorable))) {
        shapes.resetCache(patchSize)
      }
    }

    // given
    val mockGraphics = mock[GraphicsInterface]
    val mockTopologyRenderer = mock[TopologyRenderer]

    // this is the actual guy under test.
    val drawer = makeTurtleDrawer
    assert(cacheSize === 0)

    expecting {
      for (turtle <- turtles) {
        one(mockTopologyRenderer).wrapDrawable(
          if(cachableSizes.contains(turtle.size)) arg(a[CachedShape]) else arg(a[VectorShapeDrawable]),
          arg(mockGraphics),
          arg(turtle.xcor),
          arg(turtle.ycor),
          arg(turtle.size),
          arg(patchSize))
      }
    }

    when {
      for (turtle <- turtles)
        drawer.drawTurtle(mockGraphics, mockTopologyRenderer, turtle, patchSize)
    }
    def cacheSize: Int = drawer.shapes.cacheSize
  }

  case class TestTurtle(size:Double = 1,
                        var color: Object = red,
                        shape: String="test",
                        hidden: Boolean = false,
                        label: String = "",
                        labelColor: Object = white)
  extends Turtle {
    override def kind = AgentKind.Turtle
    override def classDisplayName = "TestTurtle"
    override def id = 0
    override def xcor = 0
    override def ycor = 0
    override def heading = 0
    override def lineThickness = 0
    override def hasLabel = label!=""
    override def labelString = label
    override def alpha = Color.getColor(color).getAlpha
    override def isPartiallyTransparent = { val a = alpha; a > 0 && a < 255 }
    override def world: World = sys.error("unimplemented")
    override def setVariable(vn:Int,value:Object) = sys.error("unimplemented")
    override def getVariable(vn:Int) = sys.error("unimplemented")
    override def variables = sys.error("unimplemented")
    override def heading( d:Double ) = sys.error("unimplemented")
    override def getBreed: AgentSet = sys.error("unimplemented")
    override def getBreedIndex = sys.error("unimplemented")
    override def getPatchHere = sys.error("unimplemented")
    override def jump(distance:Double) = sys.error("unimplemented")
  }
}
