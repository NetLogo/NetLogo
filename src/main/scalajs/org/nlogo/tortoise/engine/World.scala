package org.nlogo.tortoise.engine

import
  org.nlogo.tortoise.adt.{ ArrayJS, EnhancedArray, JS2ScalaConverters, NumberJS }

case class World(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) {

  private var _nextId:  Int             = 0
  private var _turtles: ArrayJS[Turtle] = ArrayJS()
  private var _patches: ArrayJS[Patch]  = ArrayJS()

  private val _width:    Int      = maxPxcor - minPxcor + 1
  private val _topology: Topology = new Torus(this, XCor(minPxcor), XCor(maxPxcor), YCor(minPycor), YCor(maxPycor))

  Overlord.flushUpdates()
  createPatches()

  def turtles  = _turtles
  def patches  = _patches
  def topology = _topology

  private def createPatches(): Unit = {

    val nested = for {
      y <- maxPycor to minPycor by -1
      x <- minPxcor to maxPxcor
    } yield {
      val id = (_width * (maxPycor - y)) + x - minPxcor
      new Patch(ID(id), this, XCor(x), YCor(y))
    }

    _patches = ArrayJS(nested: _*)

  }

  // TODO: this needs to support all topologies
  def getPatchAt(x: XCor, y: YCor): Patch = {
    import JS2ScalaConverters.num2Int
    val index = (maxPycor - StrictMathJS.round(y.value).asScala) * _width + (StrictMathJS.round(x.value).asScala - minPxcor)
    _patches(index)
  }

  def randomXCor(): XCor =
    XCor(randomCor(minPxcor, maxPxcor))

  def randomYCor(): YCor =
    YCor(randomCor(minPycor, maxPycor))

  def removeTurtle(id: ID): Unit =
    _turtles = _turtles.E filter (_.id.value != id.value)

  def clearall(): Unit = {
    Globals.init(NumberJS.toDouble(Globals.vars.length).toInt)
    _turtles.E foreach (_.die())
    createPatches()
    _nextId = 0
  }

  def getNeighbors(pxcor: XCor, pycor: YCor): ArrayJS[Patch] =
    _topology.getNeighbors(pxcor, pycor)

  def createorderedturtles(n: Int): Unit = {
    val colorFunc   = (x: Int) => NLColor((x * 10 + 5) % 140)
    val headingFunc = (x: Int) => x * (360 / n)
    createNTurtles(n, colorFunc = colorFunc, headingFunc = headingFunc)
  }

  def createturtles(n: Int): Unit =
    createNTurtles(n)

  def createturtle(x: XCor, y: YCor, color: NLColor, heading: Int): Unit = {
    _turtles.push(new Turtle(ID(_nextId), this, color, heading, x, y))
    _nextId += 1
  }

  // This method defaults to generating random turtles at (0, 0) --JAB (8/3/13)
  private[engine] def createNTurtles(n: Int, pxcor: XCor = XCor(0), pycor: YCor = YCor(0),
                                       colorFunc:   (Int) => NLColor = _ => randomColor(),
                                       headingFunc: (Int) => Int     = _ => randomHeading()): Unit = {
    0 until n foreach (x => createturtle(pxcor, pycor, colorFunc(x), headingFunc(x)))
  }

  private def randomColor(): NLColor = {
    import JS2ScalaConverters.num2Int
    NLColor(5 + 10 * RandomJS.nextInt(14).asScala)
  }

  private def randomHeading(): Int = {
    import JS2ScalaConverters.num2Int
    RandomJS.nextInt(360).asScala
  }

  private def randomCor(min: Double, max: Double): Double = {
    import JS2ScalaConverters.num2Double
    min - 0.5 + RandomJS.nextDouble().asScala * (max - min + 1)
  }

}
