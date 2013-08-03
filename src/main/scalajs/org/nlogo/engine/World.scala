package org.nlogo.engine

import scala.js.Dynamic.{ global => g }

class World(val minPxcor: Int, val maxPxcor: Int, val minPycor: Int, val maxPycor: Int) {

  private def getRandom()     = g.Random
  private def getStrictMath() = g.StrictMath

  private var _nextId:  Int         = 0
  private var _turtles: Seq[Turtle] = Seq()
  private var _patches: Seq[Patch]  = Seq()

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

    _patches = nested

  }

  // TODO: this needs to support all topologies
  def getPatchAt(x: XCor, y: YCor): Patch = {
    import Dynamic2ScalaConverters.num2Int
    val index = (maxPycor - getStrictMath().round(y.value).asScala) * _width + (getStrictMath().round(x.value).asScala - minPxcor)
    _patches(index)
  }

  def removeTurtle(id: ID): Unit =
    _turtles = _turtles.filterNot(_.id.value == id.value)

  def clearall(): Unit = {
    Globals.init(Globals.vars.length)
    _turtles foreach (_.die())
    createPatches()
    _nextId = 0
  }

  def getNeighbors(pxcor: XCor, pycor: YCor): Seq[Patch] =
    _topology.getNeighbors(pxcor, pycor)

  def createorderedturtles(n: Int): Unit = {
    val colorFunc   = (x: Int) => NLColor((x * 10 + 5) % 140)
    val headingFunc = (x: Int) => x * (360 / n)
    createNTurtles(n, colorFunc = colorFunc, headingFunc = headingFunc)
  }

  def createturtles(n: Int): Unit =
    createNTurtles(n)

  def createturtle(x: XCor, y: YCor, color: NLColor, heading: Int): Unit = {
    _turtles = _turtles :+ new Turtle(ID(_nextId), this, color, heading, x, y)
    _nextId += 1
  }

  // Defaults to generating random turtles at (0, 0)
  private[engine] def createNTurtles(n: Int, pxcor: XCor = XCor(0), pycor: YCor = YCor(0),
                                       colorFunc:   (Int) => NLColor = _ => randomColor(),
                                       headingFunc: (Int) => Int     = _ => randomHeading()): Unit = {
    0 until n foreach (x => createturtle(pxcor, pycor, colorFunc(x), headingFunc(x)))
  }

  private def randomColor(): NLColor = {
    import Dynamic2ScalaConverters.num2Int
    NLColor(5 + 10 * getRandom().nextInt(14).asScala)
  }

  private def randomHeading(): Int = {
    import Dynamic2ScalaConverters.num2Int
    getRandom().nextInt(360).asScala
  }

}
